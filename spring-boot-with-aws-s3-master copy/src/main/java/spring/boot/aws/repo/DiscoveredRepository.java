package spring.boot.aws.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import spring.boot.aws.model.DiscoveredService;

@Repository
public interface DiscoveredRepository extends JpaRepository<DiscoveredService, Long> {
	DiscoveredService save(DiscoveredService discoveredService);

	List<DiscoveredService> findByJobId(String jobId);

	List<DiscoveredService> findByJobIdAndServiceType(String jobId, String serviceType);

	List<DiscoveredService> findByServiceType(String serviceName);

	int countByServiceName(String bucketName);

	@Query("SELECT d.serviceName FROM DiscoveredService d WHERE d.serviceType = 'S3' AND d.serviceName LIKE %:pattern% AND d.serviceName = :bucketName")
	List<String> findFileNamesByPattern(@Param("bucketName") String bucketName, @Param("pattern") String pattern);
}
