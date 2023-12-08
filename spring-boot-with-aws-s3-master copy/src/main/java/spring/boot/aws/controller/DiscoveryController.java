package spring.boot.aws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.boot.aws.service.DiscoveryService;

@RestController
@RequestMapping("/discovery")
public class DiscoveryController {

	@Autowired
	private DiscoveryService discoveryService;

	@PostMapping("/services")
	public ResponseEntity<String> discoverServices(@RequestBody List<String> services) {
		String jobId = discoveryService.discoverServices(services);
		return ResponseEntity.ok(jobId);
	}

	@GetMapping("/job/{jobId}")
	public ResponseEntity<String> getJobResult(@PathVariable String jobId) {
		String status = discoveryService.getJobResult(jobId);
		return ResponseEntity.ok(status);
	}

	@GetMapping("/discoveryResult")
	public List<String> getDiscoveryResult(@RequestParam String service) {
		List<String> result = discoveryService.getDiscoveryResult(service);
		return result;
	}

	@GetMapping("/s3BucketObjects/{bucketName}")
	public ResponseEntity<String> getS3BucketObjects(@PathVariable String bucketName) {
		String jobId = discoveryService.getS3BucketObjects(bucketName);
		return ResponseEntity.ok(jobId);
	}

	@GetMapping("/s3BucketObjectCount/{bucketName}")
	public ResponseEntity<Integer> getS3BucketObjectCount(@PathVariable String bucketName) {
		int count = discoveryService.getS3BucketObjectCount(bucketName);
		return ResponseEntity.ok(count);
	}

	@GetMapping("/s3BucketObjectLike/{bucketName}")
	public ResponseEntity<?> getS3BucketObjectLike(@PathVariable String bucketName, @RequestParam String pattern) {
		try {
			List<String> matchedFiles = discoveryService.getS3BucketObjectLike(bucketName, pattern);
			return ResponseEntity.ok(matchedFiles);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error fetching S3 bucket objects: " + e.getMessage());
		}
	}

}
