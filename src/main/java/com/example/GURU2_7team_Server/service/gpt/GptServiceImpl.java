package likelion12th.centerthon.service.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion12th.centerthon.service.history.domain.GptHistory;
import likelion12th.centerthon.service.history.repository.HistoryRepository;
import likelion12th.centerthon.service.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptServiceImpl implements GptService {

    private final HistoryRepository historyRepository;
    private final InfoRepository infoRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    // gpt 사용 상황 설정
    private String systemMsg =
            "I'm going to use it as a translator to interpret sentences containing new words or memes used by the younger generation called the Mz generation these days.";

    public JsonNode callChatGpt(String question) throws JsonProcessingException {
        final String url = "https://api.openai.com/v1/chat/completions";

        String script = question
                    + " 뭐야?"
                    + "위의 문장은 요즘 mz세대 / 알파세대라고 불리는 젊은 세대들이 사용하는 신조어, 밈 등이 포함된 문장 또는 단어입니다. "
                    + "각 단어별로 끊어서 해석 후 이를 참고하여 150자 내로 요약해서 제시해 주세요. 답변은 단어별 해석을 제외한 요약한 문장만을 출력해 주세요. "
                    + "최종 문장에 큰따옴표가 있다면 작은따옴표로 변경해 주세요.";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", "gpt-4o");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", script);
        messages.add(userMessage);

        Map<String, String> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "system");
        assistantMessage.put("content", systemMsg);
        messages.add(assistantMessage);

        bodyMap.put("messages", messages);

        String body = objectMapper.writeValueAsString(bodyMap);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        return objectMapper.readTree(response.getBody());
    }


    // callChatGpt()가 반환한 JsonNode 객체에서 ChatGPT의 응답 값만 Body 값으로 출력
    @Override
    public ResponseEntity<?> getAssistantMsg(String userMsg) throws JsonProcessingException {
        JsonNode jsonNode = callChatGpt(userMsg);
        String content = jsonNode.path("choices").get(0).path("message").path("content").asText();

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }

    // 질문 / 답변 내역 저장
    @Override
    public void saveQnaHist(String question, String answer) {
        GptHistory gptHistory = new GptHistory();
        gptHistory.saveHist(question, answer);
        historyRepository.save(gptHistory);
    }

    // 추천 키워드 랜덤 추출
    @Override
    public List<String> getRecommendKeyword() {

        ArrayList<String> keywordList = new ArrayList<>();
        // 게시판에 등록된 키워드 개수
        Long keywordCount = infoRepository.count();

        // keywordCount가 7개 보다 적은 경우 빈 리스트 반환
        if (keywordCount < 7) {
            return keywordList;
        }

        // 모든 키워드 ID를 미리 조회
        List<Long> allKeywordIds = infoRepository.findAllIds(); // 이 메서드는 모든 키워드 ID를 반환한다고 가정합니다.

        Random random = new Random();
        Set<Long> selectedIds = new HashSet<>();
        int maxKeywords = 7;

        while (selectedIds.size() < maxKeywords) {
            Long randomId = allKeywordIds.get(random.nextInt(allKeywordIds.size()));
            if (selectedIds.add(randomId)) {
                infoRepository.findById(randomId).ifPresent(keyword -> keywordList.add(keyword.getWord()));
            }
        }

        return keywordList;
//        ArrayList<String> keywordList = new ArrayList<>();
//        // 게시판에 등록된 키워드 개수
//        Long keywordCount = infoRepository.count();
//        Random random = new Random();
//
//        // keywordCount가 7개 보다 적은 경우 빈 리스트 반환
//        if (keywordCount < 7) {
//            return keywordList;
//        }
//
//        // 중복 확인을 위한 해시셋
//        Set<Integer> selectedIds = new HashSet<>();
//
//        // 필요한 키워드 개수
//        int maxKeywords = 7;
//
//        while (selectedIds.size() < maxKeywords) {
//            // 1부터 keywordCount 사이의 랜덤 숫자 생성
//            Integer keywordId = random.nextInt(keywordCount.intValue()) + 1;
//            // 랜덤 추출 id 중복 확인
//            if (selectedIds.add(keywordId)) {
//                keywordList.add(infoRepository.getById(keywordId.longValue()).getWord());
//            }
//        }
//
//        return keywordList;
    }
}
