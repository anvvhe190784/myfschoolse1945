/// News Model mapped to database table `news` and backend `NewsDtos.java`.
class NewsModel {
  final int? newsId;
  final String title;
  final String summary;
  final String content;
  final String? thumbnailUrl;
  final int? authorId;
  final String? authorName;
  final String category; // 'GENERAL', 'ACADEMIC', 'EVENT', 'ACTIVITY', 'SCHOLARSHIP'
  final int viewsCount;
  final bool isPublished;
  final DateTime? publishedAt;
  final DateTime? createdAt;

  const NewsModel({
    this.newsId,
    required this.title,
    required this.summary,
    required this.content,
    this.thumbnailUrl,
    this.authorId,
    this.authorName,
    this.category = 'GENERAL',
    this.viewsCount = 0,
    this.isPublished = true,
    this.publishedAt,
    this.createdAt,
  });

  factory NewsModel.fromJson(Map<String, dynamic> json) {
    return NewsModel(
      newsId: json['newsId'] ?? json['news_id'],
      title: json['title'] ?? '',
      summary: json['summary'] ?? '',
      content: json['content'] ?? '',
      thumbnailUrl: json['thumbnailUrl'] ?? json['thumbnail_url'],
      authorId: json['authorId'] ?? json['author_id'],
      authorName: json['authorName'] ?? json['author_name'],
      category: json['category'] ?? 'GENERAL',
      viewsCount: json['viewsCount'] ?? json['views_count'] ?? 0,
      isPublished: json['isPublished'] ?? json['is_published'] ?? true,
      publishedAt: json['publishedAt'] != null
          ? DateTime.tryParse(json['publishedAt'].toString())
          : (json['published_at'] != null
              ? DateTime.tryParse(json['published_at'].toString())
              : null),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : (json['created_at'] != null
              ? DateTime.tryParse(json['created_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (newsId != null) 'newsId': newsId,
      'title': title,
      'summary': summary,
      'content': content,
      if (thumbnailUrl != null) 'thumbnailUrl': thumbnailUrl,
      if (authorId != null) 'authorId': authorId,
      if (authorName != null) 'authorName': authorName,
      'category': category,
      'viewsCount': viewsCount,
      'isPublished': isPublished,
      if (publishedAt != null) 'publishedAt': publishedAt!.toIso8601String(),
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory NewsModel.fromMap(Map<String, dynamic> map) => NewsModel.fromJson(map);

  String get formattedDate {
    if (publishedAt != null) {
      final d = publishedAt!.toLocal();
      return '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
    }
    return '';
  }

  NewsModel copyWith({
    int? newsId,
    String? title,
    String? summary,
    String? content,
    String? thumbnailUrl,
    int? authorId,
    String? authorName,
    String? category,
    int? viewsCount,
    bool? isPublished,
    DateTime? publishedAt,
    DateTime? createdAt,
  }) {
    return NewsModel(
      newsId: newsId ?? this.newsId,
      title: title ?? this.title,
      summary: summary ?? this.summary,
      content: content ?? this.content,
      thumbnailUrl: thumbnailUrl ?? this.thumbnailUrl,
      authorId: authorId ?? this.authorId,
      authorName: authorName ?? this.authorName,
      category: category ?? this.category,
      viewsCount: viewsCount ?? this.viewsCount,
      isPublished: isPublished ?? this.isPublished,
      publishedAt: publishedAt ?? this.publishedAt,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
