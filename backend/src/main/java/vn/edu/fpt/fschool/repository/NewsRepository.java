package vn.edu.fpt.fschool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.News;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByIsPublishedTrueOrderByPublishedAtDesc();
    List<News> findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(String category);
}
