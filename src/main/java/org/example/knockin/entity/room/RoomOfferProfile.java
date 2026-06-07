package org.example.knockin.entity.room;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.knockin.dto.ModifyProfileRoomInfoDto;

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

    public void updateOffer(ModifyProfileRoomInfoDto.Request request, Region region) {
        this.deposit = request.getDeposit();
        this.monthlyRent = request.getMounthRent();
        this.region = region;
        this.updateCommonInfo(request.isComeableAtNegotiable(), request.getComeEnableAt());
    }
}
