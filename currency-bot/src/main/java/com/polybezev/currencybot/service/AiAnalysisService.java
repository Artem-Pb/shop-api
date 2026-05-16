package com.polybezev.currencybot.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.polybezev.currencybot.formatter.BotMessages;
import com.polybezev.currencybot.service.TaSignalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {
    private final AnthropicClient anthropicClient;

    public String generateMorningDigest(String marketData, String news) {
        try {
            String prompt = buildPrompt(marketData, news);

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_3_7_SONNET_20250219)
                    .maxTokens(800L)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            return formatResponse(response.content().get(0).asText().text());
        } catch (Exception e) {
            log.warn("AI unavailable, returning fallback digest: {}", e.getMessage());
            return formatFallback(marketData, news);
        }
    }

    private String buildPrompt(String marketData, String news) {
        return """
        Ты — финансовый аналитик с 15-летним опытом. Каждое утро ты пишешь сводку \
        для частного инвестора: человека с портфелем, который следит за рынком, \
        умеет читать цифры, но не торгует профессионально. Он хочет понять, что \
        происходит — и что с этим делать.

        Данные на сегодня:

        РЫНОК:
        """ + marketData + """

        НОВОСТИ (последние 24 часа):
        """ + news + """

        Твоя задача — написать утреннюю сводку. Вот как это должно работать:

        СТРУКТУРА (строго):
        1. Главное за ночь — 2–3 предложения. Что изменилось, пока инвестор спал. \
           Только факты с цифрами.
        2. Почему так вышло — причины движений. Не пересказывай новости, а объясни \
           связь: что на что повлияло и почему рынок среагировал именно так.
        3. На что смотреть сегодня — 1–2 конкретных момента: событие, уровень, \
           публикация данных. Без гадания — только то, что реально важно.
        4. Одна мысль напоследок — короткий вывод. Не совет купить или продать, \
           а угол зрения: на что стоит обратить внимание при принятии решений.

        КАК ПИСАТЬ:
        — Говори как аналитик с коллегой, не как учебник со студентом.
        — Цифры и факты — основа. Без цифр нет смысла.
        — Причинно-следственные связи важнее перечисления событий.
        — Никаких клише: "рынки штормит", "инвесторы нервничают", \
          "волатильность высокая", "ситуация неоднозначная" — не пиши так.
        — Никакого канцелярита: "в рамках данного периода", "следует отметить", \
          "необходимо подчеркнуть" — выброси.
        — Если данных не хватает для вывода — скажи об этом честно, одним предложением.
        — Длина: 4 абзаца. Каждый абзац — 3–5 предложений. Не больше.

        ЗАПРЕЩЕНО:
        — Давать конкретные инвестиционные рекомендации ("купить X", "продать Y")
        — Добавлять дисклеймеры и оговорки в основной текст \
          (дисклеймер будет добавлен автоматически после)
        — Придумывать данные, которых нет во входных данных
        — Использовать заголовки и маркированные списки внутри сводки — \
          только связный текст
        """;
    }

    private String formatResponse(String aiResponse) {
        return aiResponse + "\n\n" +
                "_⚠️ Не является инвестиционной рекомендацией. " +
                "Материал носит информационный характер._";
    }

    private String formatFallback(String marketData, String news) {
        return BotMessages.DIGEST_FALLBACK
                .replace("{marketData}", marketData)
                .replace("{news}", news);
    }

    /**
     * Генерирует краткое AI-объяснение TA-сигнала (2-3 предложения).
     * При ошибке возвращает null — вызывающий код показывает только TA без объяснения.
     */
    public String generateSignalExplanation(TaSignalService.SignalResult r) {
        try {
            String prompt = String.format(
                    "Ты трейдинговый аналитик. Объясни простым языком (2-3 предложения) что значат эти показатели:\n" +
                    "Монета: %s\n" +
                    "Итог: %s\n" +
                    "RSI(14): %.1f\n" +
                    "MACD: %.6f, сигнальная линия: %.6f\n\n" +
                    "Без заголовков, без списков. Только связный текст. " +
                    "Не давай конкретных советов купить/продать.",
                    r.coin(), r.signal(), r.rsi(), r.macd(), r.macdSignal()
            );

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_3_7_SONNET_20250219)
                    .maxTokens(200L)
                    .addUserMessage(prompt)
                    .build();

            Message response = anthropicClient.messages().create(params);
            return "_" + response.content().get(0).asText().text().trim() + "_";
        } catch (Exception e) {
            log.warn("Signal explanation AI unavailable: {}", e.getMessage());
            return null;
        }
    }
}
