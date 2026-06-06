package org.example.knockin.entity.room;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "room_offer_profile")
@DiscriminatorValue(RoomProfileType.Values.OFFER)
public class RoomOfferProfile extends RoomProfile {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "monthly_rent", nullable = false)
    private Integer monthlyRent;
}
