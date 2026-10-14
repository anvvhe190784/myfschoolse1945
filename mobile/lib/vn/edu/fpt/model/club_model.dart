/// Club Model mapped to database table `clubs` and backend `ClubDtos.java`.
class ClubModel {
  final int? clubId;
  final String clubName;
  final String description;
  final String category; // 'ACADEMIC', 'ARTS', 'SPORTS', 'VOLUNTEER', 'TECHNOLOGY', 'MEDIA', 'OTHER'
  final String? avatarUrl;
  final String? bannerUrl;
  final int? leaderId;
  final String? leaderName;
  final String? contactEmail;
  final int memberCount;
  final bool isActive;
  final DateTime? createdAt;

  const ClubModel({
    this.clubId,
    required this.clubName,
    required this.description,
    required this.category,
    this.avatarUrl,
    this.bannerUrl,
    this.leaderId,
    this.leaderName,
    this.contactEmail,
    this.memberCount = 1,
    this.isActive = true,
    this.createdAt,
  });

  factory ClubModel.fromJson(Map<String, dynamic> json) {
    return ClubModel(
      clubId: json['clubId'] ?? json['club_id'],
      clubName: json['clubName'] ?? json['club_name'] ?? '',
      description: json['description'] ?? '',
      category: json['category'] ?? 'OTHER',
      avatarUrl: json['avatarUrl'] ?? json['avatar_url'],
      bannerUrl: json['bannerUrl'] ?? json['banner_url'],
      leaderId: json['leaderId'] ?? json['leader_id'],
      leaderName: json['leaderName'] ?? json['leader_name'],
      contactEmail: json['contactEmail'] ?? json['contact_email'],
      memberCount: json['memberCount'] ?? json['member_count'] ?? 1,
      isActive: json['isActive'] ?? json['is_active'] ?? true,
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : (json['created_at'] != null
              ? DateTime.tryParse(json['created_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (clubId != null) 'clubId': clubId,
      'clubName': clubName,
      'description': description,
      'category': category,
      if (avatarUrl != null) 'avatarUrl': avatarUrl,
      if (bannerUrl != null) 'bannerUrl': bannerUrl,
      if (leaderId != null) 'leaderId': leaderId,
      if (leaderName != null) 'leaderName': leaderName,
      if (contactEmail != null) 'contactEmail': contactEmail,
      'memberCount': memberCount,
      'isActive': isActive,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory ClubModel.fromMap(Map<String, dynamic> map) => ClubModel.fromJson(map);

  ClubModel copyWith({
    int? clubId,
    String? clubName,
    String? description,
    String? category,
    String? avatarUrl,
    String? bannerUrl,
    int? leaderId,
    String? leaderName,
    String? contactEmail,
    int? memberCount,
    bool? isActive,
    DateTime? createdAt,
  }) {
    return ClubModel(
      clubId: clubId ?? this.clubId,
      clubName: clubName ?? this.clubName,
      description: description ?? this.description,
      category: category ?? this.category,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      bannerUrl: bannerUrl ?? this.bannerUrl,
      leaderId: leaderId ?? this.leaderId,
      leaderName: leaderName ?? this.leaderName,
      contactEmail: contactEmail ?? this.contactEmail,
      memberCount: memberCount ?? this.memberCount,
      isActive: isActive ?? this.isActive,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

/// Club Membership Model mapped to database table `club_memberships`.
class ClubMembershipModel {
  final int? membershipId;
  final int clubId;
  final int userId;
  final String role; // 'LEADER', 'VICE_LEADER', 'CORE_MEMBER', 'MEMBER'
  final DateTime? joinedAt;

  const ClubMembershipModel({
    this.membershipId,
    required this.clubId,
    required this.userId,
    this.role = 'MEMBER',
    this.joinedAt,
  });

  factory ClubMembershipModel.fromJson(Map<String, dynamic> json) {
    return ClubMembershipModel(
      membershipId: json['membershipId'] ?? json['membership_id'],
      clubId: json['clubId'] ?? json['club_id'] ?? 0,
      userId: json['userId'] ?? json['user_id'] ?? 0,
      role: json['role'] ?? 'MEMBER',
      joinedAt: json['joinedAt'] != null
          ? DateTime.tryParse(json['joinedAt'].toString())
          : (json['joined_at'] != null
              ? DateTime.tryParse(json['joined_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (membershipId != null) 'membershipId': membershipId,
      'clubId': clubId,
      'userId': userId,
      'role': role,
      if (joinedAt != null) 'joinedAt': joinedAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory ClubMembershipModel.fromMap(Map<String, dynamic> map) =>
      ClubMembershipModel.fromJson(map);

  ClubMembershipModel copyWith({
    int? membershipId,
    int? clubId,
    int? userId,
    String? role,
    DateTime? joinedAt,
  }) {
    return ClubMembershipModel(
      membershipId: membershipId ?? this.membershipId,
      clubId: clubId ?? this.clubId,
      userId: userId ?? this.userId,
      role: role ?? this.role,
      joinedAt: joinedAt ?? this.joinedAt,
    );
  }
}
