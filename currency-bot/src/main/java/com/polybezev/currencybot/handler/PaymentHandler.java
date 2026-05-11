package com.polybezev.currencybot.handler;

import com.polybezev.currencybot.formatter.BotMessages;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentHandler {
    private final SubscriptionService subscriptionService;

    public SendInvoice sendInvoice(long chatId, Tier tier) {
        SendInvoice invoice = new SendInvoice() {
            @Override
            public void validate() throws TelegramApiValidationException {
                if (getChatId() == null)
                    throw new TelegramApiValidationException("ChatId empty", this);
                if (getTitle() == null)
                    throw new TelegramApiValidationException("Title empty", this);
                if (getPayload() == null)
                    throw new TelegramApiValidationException("Payload empty", this);
                if (getPrices() == null || getPrices().isEmpty())
                    throw new TelegramApiValidationException("Prices empty", this);
                // providerToken не проверяем — для XTR (Telegram Stars) не требуется
            }
        };
        invoice.setChatId(chatId);
        invoice.setTitle(tier.label);
        invoice.setDescription("Подписка на 30 дней");
        invoice.setPayload(tier.name());
        invoice.setProviderToken("");
        invoice.setCurrency("XTR");
        invoice.setPrices(List.of(new LabeledPrice(tier.label, tier.starsPrice)));
        return invoice;
    }

    public SendMessage handleSuccessfulPayment(long chatId, SuccessfulPayment payment) {
        Tier tier = Tier.valueOf(payment.getInvoicePayload());
        subscriptionService.activateSubscription(chatId, tier, payment.getTotalAmount());
        String text = BotMessages.STARS_SUCCESS.replace("{tierName}", tier.label);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }
}
