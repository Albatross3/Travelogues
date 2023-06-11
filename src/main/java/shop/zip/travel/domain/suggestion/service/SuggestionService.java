package shop.zip.travel.domain.suggestion.service;

import static java.util.stream.Collectors.groupingBy;

import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.zip.travel.domain.suggestion.SuggestionCreateDto;
import shop.zip.travel.domain.suggestion.entity.Suggestion;
import shop.zip.travel.domain.suggestion.repository.SuggestionRepository;

@Service
@Transactional(readOnly = true)
public class SuggestionService {

  private static final String DEFAULT_COUNTRY = "대한민국";

  private final SuggestionRepository suggestionRepository;

  public SuggestionService(SuggestionRepository suggestionRepository) {
    this.suggestionRepository = suggestionRepository;
  }

  @Transactional
  public void save(SuggestionCreateDto suggestionDto) {
    Suggestion suggestion = new Suggestion(
        suggestionDto.travelogueId(),
        suggestionDto.countryName(),
        suggestionDto.title(),
        suggestionDto.thumbnail(),
        suggestionDto.totalCost(),
        suggestionDto.memberId());

    suggestionRepository.save(suggestion);
  }

  public String getTopSuggestionCountry(Long memberId) {
    return suggestionRepository.getSuggestionByMemberId(memberId)
        .stream()
        .collect(groupingBy(Suggestion::getCountryName, Collectors.counting()))
        .entrySet()
        .stream()
        .max(Entry.comparingByValue())
        .map(Entry::getKey)
        .orElse(DEFAULT_COUNTRY);
  }

}