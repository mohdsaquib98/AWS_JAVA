package spring.boot.aws.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import spring.boot.aws.model.DiscoveredService;
import spring.boot.aws.repo.DiscoveredRepository;

@Service
public class DiscoveryServiceImpl implements DiscoveryService {

	@Autowired
	private AmazonS3 amazonS3;

	@Autowired
	private AmazonEC2 amazonEC2;

	DiscoveredService discoveredService = new DiscoveredService();

	@Autowired
	private DiscoveredRepository discoveredRepository;

	@Override
	@Async
	public String discoverServices(List<String> services) {
		String jobId = generateJobId();

		for (String service : services) {
			if ("EC2".equalsIgnoreCase(service)) {
				discoverEC2Instances(jobId);
			} else if ("S3".equalsIgnoreCase(service)) {
				discoverS3Buckets(jobId);
			}
		}

		return jobId;
	}

	private void discoverEC2Instances(String jobId) {
		try {
			DescribeInstancesRequest request = new DescribeInstancesRequest();
			DescribeInstancesResult response = amazonEC2.describeInstances(request);
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					DiscoveredService ec2Service = new DiscoveredService();
					ec2Service.setServiceType("EC2");
					ec2Service.setServiceName(instance.getInstanceId());
					ec2Service.setJobId(jobId);
					ec2Service.setState("Success");
					discoveredRepository.save(ec2Service);
				}
			}
			System.out.println("EC2 discovery complete.");
		} catch (Exception e) {
			System.out.println("EC2 discovery failed: " + e.getMessage());
			updateServiceState(jobId, "EC2", "Failed");
		}
	}

	private void discoverS3Buckets(String jobId) {
		try {
			List<Bucket> buckets = amazonS3.listBuckets();
			for (Bucket bucket : buckets) {
				DiscoveredService s3Service = new DiscoveredService();
				s3Service.setServiceType("S3");
				s3Service.setServiceName(bucket.getName());
				s3Service.setJobId(jobId);
				s3Service.setState("Success");
				discoveredRepository.save(s3Service);
			}
			System.out.println("S3 bucket discovery complete.");
		} catch (Exception e) {
			System.out.println("S3 bucket discovery failed: " + e.getMessage());
			updateServiceState(jobId, "S3", "Failed");
		}
	}

	private String generateJobId() {
		return UUID.randomUUID().toString();
	}

	private void updateServiceState(String jobId, String serviceType, String state) {
		List<DiscoveredService> services = discoveredRepository.findByJobIdAndServiceType(jobId, serviceType);
		for (DiscoveredService service : services) {
			service.setState(state);
			discoveredRepository.save(service);
		}
	}

	@Override
	public String getJobResult(String jobId) {
		List<DiscoveredService> discoveredServices = discoveredRepository.findByJobId(jobId);

		boolean hasSuccess = discoveredServices.stream()
				.anyMatch(service -> "Success".equalsIgnoreCase(service.getState()));
		boolean hasFailed = discoveredServices.stream()
				.anyMatch(service -> "Failed".equalsIgnoreCase(service.getState()));

		if (hasSuccess) {
			return "Success";
		} else if (hasFailed) {
			return "Failed";
		} else {
			return "In Progress";
		}
	}

	@Override
	public List<String> getDiscoveryResult(String serviceName) {
		List<DiscoveredService> discoveredServices = discoveredRepository.findByServiceType(serviceName);

		List<String> result = new ArrayList<>();

		for (DiscoveredService service : discoveredServices) {
			if ("S3".equalsIgnoreCase(serviceName)) {
				if ("S3".equalsIgnoreCase(service.getServiceType())) {
					result.add(service.getServiceName());
				}
			} else if ("EC2".equalsIgnoreCase(serviceName)) {
				if ("EC2".equalsIgnoreCase(service.getServiceType())) {
					result.add(service.getServiceName());
				}
			}
		}

		return result;
	}

	@Override
	@Async
	public String getS3BucketObjects(String bucketName) {
		String jobId = generateJobId();
		try {
			List<S3ObjectSummary> s3ObjectSummaries = amazonS3.listObjectsV2(bucketName).getObjectSummaries();

			for (S3ObjectSummary objectSummary : s3ObjectSummaries) {
				String fileName = objectSummary.getKey();
				DiscoveredService s3File = new DiscoveredService();
				s3File.setServiceType("S3");
				s3File.setServiceName(fileName);
				s3File.setJobId(jobId);
				discoveredRepository.save(s3File);
			}

			System.out.println("S3 bucket objects discovery complete.");
		} catch (AmazonS3Exception e) {
			updateServiceState(jobId, "S3", "Failed");
			System.err.println("Error fetching S3 bucket objects: " + e.getMessage());
		}
		return jobId;
	}

	@Override
	public int getS3BucketObjectCount(String bucketName) {
		return discoveredRepository.countByServiceName(bucketName);
	}

	@Override
	public List<String> getS3BucketObjectLike(String bucketName, String pattern) {
		return discoveredRepository.findFileNamesByPattern(bucketName, pattern);
	}

}
