package repository;



import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import model.QueryTask;

public interface QueryRepository extends JpaRepository<QueryTask, Long> {
	List<QueryTask> findByRaiserName(String raiserName);
}
