package mhalo.parking.service.domain;

import mhalo.domain.model.event.DomainConstants;
import mhalo.domain.model.event.model.Interval;
import mhalo.domain.model.event.model.Money;
import mhalo.domain.model.event.model.Rate;
import mhalo.parking.service.domain.model.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestParkingFeeCalculator {

    private ParkingFeeCalculator underTest;

    @BeforeEach
    void setup() {
        this.underTest = new ParkingFeeCalculator();
    }

    @Nested
    class CalculateStartingFee {

        @ParameterizedTest(name = "{index}. {3} => expectedStartingFee: {2}")
        @MethodSource("calculateStartingFeeSource")
        void should_CalculateStartingFee(Instant parkingStartAt, Zone parkingZone, Money expectedStartingFee, String description) {
            //given (empty)

            //when
            Money actualStartingFee = underTest.calculateStartingFee(parkingStartAt, parkingZone);

            //then
            assertEquals(expectedStartingFee, actualStartingFee);
        }

        private static Stream<Arguments> calculateStartingFeeSource() {
            Zone zone = Zone.builder()
                    .id(UUID.randomUUID())
                    .payInterval(new Interval(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                    .rate(Rate.builder().duration(Duration.ofHours(1)).amount(new Money(BigDecimal.valueOf(100))).build()).build();

            return Stream.of(
                    Arguments.of(instantAtLocalTime(LocalTime.of(22, 0)), zone, Money.ZERO, "Currently Free zone test"),
                    Arguments.of(instantAtLocalTime(LocalTime.of(12, 0)), zone, new Money(BigDecimal.valueOf(800)), "Paid interval test"),
                    Arguments.of(instantAtLocalTime(LocalTime.of(19, 0)), zone, new Money(BigDecimal.valueOf(100)), "Paid interval test"),
                    Arguments.of(instantAtLocalTime(LocalTime.of(19, 30)), zone, new Money(BigDecimal.valueOf(50)), "Paid interval test"),
                    Arguments.of(instantAtLocalTime(LocalTime.of(19, 40)), zone, new Money(BigDecimal.valueOf(33.33)), "Paid interval test"));
        }
    }

    @Nested
    class CalculateClosingFee {

        @ParameterizedTest
        @MethodSource("calculateClosingFeeSource")
        void should_CalculateClosingFee(CalculateClosingFeeTestArgs calculateClosingFeeTestArgs) {
            //given

            //when
            Money actualClosingFee = underTest.calculateClosingFee(calculateClosingFeeTestArgs.parkingZone, calculateClosingFeeTestArgs.parkingStartedAt,
                    calculateClosingFeeTestArgs.parkingStoppedAt, calculateClosingFeeTestArgs.startingFee);

            //then
            assertEquals(calculateClosingFeeTestArgs.expectedClosingFee(), actualClosingFee);
        }

        record CalculateClosingFeeTestArgs(Instant parkingStartedAt, Instant parkingStoppedAt, Money startingFee,
                                           Zone parkingZone, Money expectedClosingFee, String description){}

        private static Stream<Arguments> calculateClosingFeeSource() {
            Zone zone = Zone.builder()
                    .id(UUID.randomUUID())
                    .payInterval(new Interval(LocalTime.of(10, 0), LocalTime.of(20, 0)))
                    .rate(Rate.builder().duration(Duration.ofHours(1)).amount(new Money(BigDecimal.valueOf(100))).build()).build();

            return Stream.of(
                    Arguments.of(
                        new CalculateClosingFeeTestArgs(
                                instantAtLocalDateTime(LocalTime.of(12, 0), LocalDate.of(2010,1,1)),
                                instantAtLocalDateTime(LocalTime.of(12, 0), LocalDate.of(2010,1,2)),
                                new Money(BigDecimal.valueOf(500)), zone, new Money(BigDecimal.valueOf(500)), "Stopped on next day")),
                    Arguments.of(
                        new CalculateClosingFeeTestArgs(
                            instantAtLocalDateTime(LocalTime.of(12, 0), LocalDate.of(2010,1,1)),
                            instantAtLocalDateTime(LocalTime.of(22, 0), LocalDate.of(2010,1,1)),
                            new Money(BigDecimal.valueOf(500)), zone, new Money(BigDecimal.valueOf(500)), "Stopped on same day, after paid interval")),
                    Arguments.of(
                        new CalculateClosingFeeTestArgs(
                            instantAtLocalDateTime(LocalTime.of(12, 0), LocalDate.of(2010,1,1)),
                            instantAtLocalDateTime(LocalTime.of(13, 0), LocalDate.of(2010,1,1)),
                            new Money(BigDecimal.valueOf(500)), zone, new Money(BigDecimal.valueOf(100)), "Stopped on same day, in paid interval")));
        }
    }

    private static Instant instantAtLocalTime(LocalTime localTime) {
        return ZonedDateTime.of(
                LocalDate.of(2010,1,1), localTime,
                ZoneId.of(DomainConstants.UTC)).toInstant();
    }

    private static Instant instantAtLocalDateTime(LocalTime localTime, LocalDate localDate) {
        return ZonedDateTime.of(
                localDate, localTime,
                ZoneId.of(DomainConstants.UTC)).toInstant();
    }

}
