package vn.edu.fpt.fschool.dto;

public class ClubDtos {
    public record ClubResponse(
            Long clubId,
            String clubName,
            String description,
            String category,
            String avatarUrl,
            String bannerUrl,
            String leaderName,
            String contactEmail,
            Integer memberCount,
            Boolean isActive
    ) {}
}
