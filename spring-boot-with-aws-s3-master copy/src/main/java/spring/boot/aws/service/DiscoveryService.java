package spring.boot.aws.service;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface DiscoveryService {

	String discoverServices(List<String> services);

	String getJobResult(String jobId);

	String getS3BucketObjects(String bucketName);

	int getS3BucketObjectCount(String bucketName);

	List<String> getS3BucketObjectLike(String bucketName, String pattern);

	List<String> getDiscoveryResult(String serviceName);
}
