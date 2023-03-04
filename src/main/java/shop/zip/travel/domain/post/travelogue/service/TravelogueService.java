package shop.zip.travel.domain.post.travelogue.service;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.zip.travel.domain.member.entity.Member;
import shop.zip.travel.domain.member.service.MemberService;
import shop.zip.travel.domain.post.travelogue.dto.TravelogueSimple;
import shop.zip.travel.domain.post.travelogue.dto.req.TravelogueCreateReq;
import shop.zip.travel.domain.post.travelogue.dto.res.TravelogueCreateRes;
import shop.zip.travel.domain.post.travelogue.dto.res.TravelogueCustomSlice;
import shop.zip.travel.domain.post.travelogue.dto.res.TravelogueDetailRes;
import shop.zip.travel.domain.post.travelogue.dto.res.TravelogueSimpleRes;
import shop.zip.travel.domain.post.travelogue.entity.Travelogue;
import shop.zip.travel.domain.post.travelogue.exception.TravelogueNotFoundException;
import shop.zip.travel.domain.post.travelogue.repository.TravelogueRepository;
import shop.zip.travel.global.error.ErrorCode;

@Service
@Transactional(readOnly = true)
public class TravelogueService {

    private final TravelogueRepository travelogueRepository;
    private final MemberService memberService;

    public TravelogueService(TravelogueRepository travelogueRepository,
        MemberService memberService) {
        this.travelogueRepository = travelogueRepository;
        this.memberService = memberService;
    }

	@Transactional
	public TravelogueCreateRes save(TravelogueCreateReq createReq, Long memberId) {
		Member findMember = memberService.getMember(memberId);
		Travelogue travelogue = travelogueRepository.save(createReq.toTravelogue(findMember));
		Long nights = travelogue.getPeriod().getNights();
		return new TravelogueCreateRes(travelogue.getId(), nights, nights + 1);
	}

	public TravelogueCustomSlice<TravelogueSimpleRes> getTravelogues(int page, int size, String sortField) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortField));

		Slice<TravelogueSimple> travelogues = travelogueRepository.findAllBySlice(pageRequest);

		return TravelogueCustomSlice.toDto(
			travelogues.map(TravelogueSimpleRes::toDto)
		);
	}

	public Travelogue getTravelogue(Long id) {
		return travelogueRepository.findById(id)
				.orElseThrow(() -> new TravelogueNotFoundException(ErrorCode.TRAVELOGUE_NOT_FOUND));
	}

	public List<TravelogueSimpleRes> search(Long lastTravelogue, String keyword, String orderType,
			int size) {
		return travelogueRepository.search(lastTravelogue, keyword, orderType, size);
	}

	@Transactional
	public TravelogueDetailRes getTravelogueDetail(Long travelogueId, boolean canAddViewCount) {
		setViewCount(travelogueId, canAddViewCount);
		return TravelogueDetailRes.toDto(
				travelogueRepository.getTravelogueDetail(travelogueId)
						.orElseThrow(() -> new TravelogueNotFoundException(ErrorCode.TRAVELOGUE_NOT_FOUND)));
	}

	private void setViewCount(Long travelogueId, boolean canAddViewCount) {
		if (canAddViewCount) {
			Travelogue findTravelogue = getTravelogue(travelogueId);
			findTravelogue.addViewCount();
		}
	}

}

