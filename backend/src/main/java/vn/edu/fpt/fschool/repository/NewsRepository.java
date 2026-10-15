package vn.edu.fpt.fschool.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.fschool.model.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
  List<News> findByIsPublishedTrueOrderByPublishedAtDesc();

  Page<News> findByIsPublishedTrueOrderByPublishedAtDesc(Pageable pageable);

  List<News> findByCategoryAndIsPublishedTrueOrderByPublishedAtDesc(String category);

  @Modifying
  @Query("UPDATE News n SET n.viewsCount = n.viewsCount + :delta WHERE n.newsId = :newsId")
  void incrementViewsCount(@Param("newsId") Long newsId, @Param("delta") Long delta);
}
