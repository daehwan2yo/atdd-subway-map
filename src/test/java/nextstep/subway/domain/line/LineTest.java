package nextstep.subway.domain.line;

import nextstep.subway.domain.section.Section;
import nextstep.subway.domain.station.Station;
import nextstep.subway.handler.error.custom.BusinessException;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nextstep.subway.domain.factory.EntityFactory.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("노선 단위 테스트")
class LineTest {

    private Station 강남역;
    private Station 역삼역;

    @BeforeEach
    void init() {
        강남역 = createStation("강남역");
        역삼역  = createStation("역삼역");
    }

    /**
     * 노선이 생성되고 여러 구간이 존재할때
     * 노선의 역 목록을 조회하면
     * 최상행선부터 최하행선까지 순서대로 조회가 가능합니다.
     */
    @Test
    @DisplayName("노선의 역 순서대로 조회")
    void getStationList() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Station 선릉역 = createStation("선릉역");
        Station 교대역 = createStation("교대역");
        Station 삼성역 = createStation("삼성역");

        이호선.addSection(createSection(역삼역, 선릉역, 4));
        이호선.addSection(createSection(선릉역, 교대역, 5));
        이호선.addSection(createSection(교대역, 삼성역, 7));

        // when/then
        assertThat(이호선.getStationList()).containsExactly(Arrays.array(강남역, 역삼역, 선릉역, 교대역, 삼성역));
    }

    /**
     * 노선이 생성되었을때,
     * 노선의 정보 변경이 가능합니다.
     */
    @Test
    @DisplayName("노선의 정보를 변경한다.")
    void modify() {
        // given
        Line line = createLine("2호선", "green", 강남역, 역삼역, 10);

        // when
        line.modify("3호선", "orange");

        // then
        assertThat(line.toString()).contains("3호선", "orange");
    }

    /**
     * 노선이 생성되었을때,
     * 노선의 정보 변경시 null 혹은 기존값을 입력하면 해당 값은 변경되지 않아야 합니다.
     */
    @Test
    @DisplayName("노선 정보 수정시 null 혹은 기존 값이 입력되면 변경을 하지 않는다.")
    void validationOfModify() {
        // given
        Line line = createLine("2호선", "green", 강남역, 역삼역, 10);

        // when
        line.modify("3호선", null);

        // then
        assertThat(line.toString()).contains("3호선", "green");
    }

    /**
     * 노선과 역이 생성되었을때, 새로운 역과 구간 추가시
     * 구간을 추가할 수 있습니다.
     */
    @Test
    @DisplayName("노선에 구간을 등록한다.")
    void addSection() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Station 선릉역 = createStation("선릉역");

        // when
        이호선.addSection(createSection(역삼역, 선릉역, 8));

        // then
        assertThat(이호선.getStationList()).containsExactly(Arrays.array(강남역, 역삼역, 선릉역));
    }

    /**
     * 한 구간이 존재하고 새로운 구간이 추가될때,
     * 상행역이 같고 하행역이 다르면
     * 기존 구간은 쪼개지고 새로운 구간은 추가된다.
     * (기존구간_상행 - 새로운구간_하행) & (새로운구간_하행 - 기존구간_하행)
     */
    @Test
    @DisplayName("구간 사이에 새로운 구간을 추가한다. - 상행선이 같은 경우")
    void addSection2() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Station 선릉역 = createStation("선릉역");
        Section section = createSection(강남역, 선릉역, 8);

        // when
        이호선.addSection(section);

        // then
        assertThat(이호선.getStationList()).containsExactly(Arrays.array(강남역, 선릉역, 역삼역));
    }

    @Test
    @DisplayName("구간 사이에 새로운 구간을 추가한다. - 하행선이 같은 경우")
    void addSection3() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Station 선릉역 = createStation("선릉역");
        Section section = createSection(선릉역, 역삼역, 8);

        // when
        이호선.addSection(section);

        // then
        assertThat(이호선.getStationList()).containsExactly(Arrays.array(강남역, 선릉역, 역삼역));
    }

    /**
     * 노선에 구간이 존재할때, 새로운 구간을 생성하면
     * 새로운 구간의 하행선이 노선의 최상행과 같다면
     * 구간을 추가하고 최상행역을 갱신시켜준다.
     */
    @Test
    @DisplayName("구간 사이에 새로운 구간을 추가한다. - 하행역이 노선의 최상행역과 같은 경우")
    void addSection4() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Station 선릉역 = createStation("선릉역");
        Section section = createSection(선릉역, 강남역, 8);

        // when
        이호선.addSection(section);

        // then
        assertThat(이호선.getStationList()).containsExactly(Arrays.array(선릉역, 강남역, 역삼역));
    }

    /**
     * 노선에 구간이 존재할때, 새로운 구간을 생성하면
     * 새로운 구간의 거리가 기존 구간의 거리보다 크거나 같으면
     * 예외를 발생한다.
     */
    @Test
    @DisplayName("구간 사이에 새로운 구간을 추가한다. - 기존구간의 거리보다 크거나 같은경우")
    void addSection5() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Station 선릉역 = createStation("선릉역");
        Section biggerDistanceSection = createSection(강남역, 선릉역, 11);
        Section sameDistanceSection = createSection(강남역, 선릉역, 10);

        // when / then
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> 이호선.addSection(biggerDistanceSection))
                .withMessageStartingWith("[ERROR]");
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> 이호선.addSection(sameDistanceSection))
                .withMessageStartingWith("[ERROR]");
    }

    /**
     * 노선과 구간이 생성되었을때,
     * 입력된 역이 노선에 존재하는지 확인합니다.
     */
    @Test
    @DisplayName("입력된 역이 노선에 존재하는지 확인한다.")
    void hasStation() {
        // given
        Line completeLine = createLine("2호선", "green", 강남역, 역삼역, 10);

        // when/then
        assertThat(completeLine.hasStation(강남역)).isTrue();
    }

    /**
     * 노선이 생성되고 여러개의 구간이 생성되었을때,
     * 노선의 구간 목록들을 조회합니다.
     */
    @Test
    @DisplayName("노선의 구간목록을 조회한다.")
    void getSectionList() {
        // given
        Station 선릉역 = createStation("선릉역");
        Section 역삼_선릉_구간 = createSection(역삼역, 선릉역, 8);

        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        이호선.addSection(역삼_선릉_구간);

        // when
        List<Section> sectionList = 이호선.getSectionList();

        // then
        assertThat(sectionList.size()).isEqualTo(2);
    }

    /**
     * 노선이 생성되고 여러 구간에 대해서
     * 노선에 존재하는 구간을 삭제합니다.
     */
    @Test
    @DisplayName("노선의 구간을 삭제한다.")
    void deleteSection() {
        // given
        Station 선릉역 = createStation("선릉역");

        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);
        Section newSection = createSection(역삼역, 선릉역, 10);
        이호선.addSection(newSection);

        // when
        이호선.deleteSection(newSection);

        // then
        assertThat(이호선.getSectionList().size()).isEqualTo(1);
    }

    /**
     * 노선을 삭제할때,
     * 구간이 하나만 존재하면 구간 삭제가 불가능합니다.
     */
    @Test
    @DisplayName("노선에 구간이 하나만 있으면 구간 삭제가 불가능합니다.")
    void validationOfDeleteSection() {
        // given
        Line 이호선 = createLine("2호선", "green", 강남역, 역삼역, 10);

        // when / then
        Section section = 이호선.getSectionList().get(0);
        assertThatThrownBy(() -> 이호선.deleteSection(section))
                .isInstanceOf(BusinessException.class);
    }
}