package mhalo.parking.service.httpclient.zone.mapper;

import mhalo.domain.model.event.model.Interval;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.Rate;
import mhalo.httpclient.zone.FindZoneByIdResponse;
import mhalo.httpclient.zone.IntervalDto;
import mhalo.httpclient.zone.RateDto;
import mhalo.parking.service.domain.model.Zone;
import org.springframework.stereotype.Component;

@Component
public class ZoneHttpClientMapper {

    public Zone mapFindZoneByIdResponseToZone(FindZoneByIdResponse findZoneByIdResponse) {
        IntervalDto intervalDto = findZoneByIdResponse.getPayInterval();
        RateDto rateDto = findZoneByIdResponse.getRate();
        return Zone.builder()
                .id(findZoneByIdResponse.getZoneId())
                .rate(Rate.builder()
                        .duration(rateDto.getDuration())
                        .amount(new Money(rateDto.getAmount()))
                        .build())
                .payInterval(new Interval(intervalDto.getStartTime(), intervalDto.getEndTime()))
                .build();
    }
}
