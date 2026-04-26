package dev.jayav.patterns.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Factory Pattern implementation.
 *
 * @author Jaya V
 * @version 1.0.0
 */
@DisplayName("Factory Pattern — Notification Service Tests")
class FactoryPatternTest {

    @Nested
    @DisplayName("NotificationServiceFactory")
    class FactoryTests {

        @Test
        @DisplayName("should create EmailNotification for EMAIL channel")
        void shouldCreateEmailService() {
            Optional<NotificationService> service =
                    NotificationServiceFactory.create(NotificationChannel.EMAIL);

            assertThat(service).isPresent();
            assertThat(service.get()).isInstanceOf(EmailNotification.class);
            assertThat(service.get().getChannel()).isEqualTo(NotificationChannel.EMAIL);
        }

        @Test
        @DisplayName("should create SmsNotification for SMS channel")
        void shouldCreateSmsService() {
            Optional<NotificationService> service =
                    NotificationServiceFactory.create(NotificationChannel.SMS);

            assertThat(service).isPresent();
            assertThat(service.get()).isInstanceOf(SmsNotification.class);
            assertThat(service.get().getChannel()).isEqualTo(NotificationChannel.SMS);
        }

        @Test
        @DisplayName("should create PushNotification for PUSH channel")
        void shouldCreatePushService() {
            Optional<NotificationService> service =
                    NotificationServiceFactory.create(NotificationChannel.PUSH);

            assertThat(service).isPresent();
            assertThat(service.get()).isInstanceOf(PushNotification.class);
            assertThat(service.get().getChannel()).isEqualTo(NotificationChannel.PUSH);
        }

        @Test
        @DisplayName("should return Optional.empty() for null channel")
        void shouldReturnEmptyForNullChannel() {
            Optional<NotificationService> service =
                    NotificationServiceFactory.create(null);

            assertThat(service).isEmpty();
        }

        @Test
        @DisplayName("createOrThrow should throw for null channel")
        void createOrThrowShouldThrowForNull() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> NotificationServiceFactory.createOrThrow(null));
        }

        @Test
        @DisplayName("factory class should not be instantiable")
        void factoryClassShouldNotBeInstantiable() throws Exception {
            var constructor = NotificationServiceFactory.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatExceptionOfType(java.lang.reflect.InvocationTargetException.class)
                    .isThrownBy(constructor::newInstance)
                    .havingCause().isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("EmailNotification")
    class EmailNotificationTests {

        private final NotificationService service =
                NotificationServiceFactory.createOrThrow(NotificationChannel.EMAIL);

        @Test
        @DisplayName("should deliver to a valid email address")
        void shouldDeliverToValidEmail() {
            NotificationResult result = service.send("test@example.com", "Hello!");

            assertThat(result.channel()).isEqualTo(NotificationChannel.EMAIL);
            assertThat(result.recipient()).isEqualTo("test@example.com");
            assertThat(result.messageId()).isNotBlank();
            assertThat(result.deliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("should fail for invalid email address")
        void shouldFailForInvalidEmail() {
            NotificationResult result = service.send("not-an-email", "Hello!");

            assertThat(result.status()).isEqualTo(DeliveryStatus.FAILED);
            assertThat(result.detail()).contains("Invalid email address");
        }

        @Test
        @DisplayName("isAvailable should return true for configured service")
        void shouldBeAvailableWhenConfigured() {
            assertThat(service.isAvailable()).isTrue();
        }
    }

    @Nested
    @DisplayName("SmsNotification")
    class SmsNotificationTests {

        private final NotificationService service =
                NotificationServiceFactory.createOrThrow(NotificationChannel.SMS);

        @Test
        @DisplayName("should deliver to valid E.164 phone number")
        void shouldDeliverToValidPhone() {
            NotificationResult result = service.send("+14155552671", "Your OTP is 123456");

            assertThat(result.channel()).isEqualTo(NotificationChannel.SMS);
            assertThat(result.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("should fail for invalid phone number format")
        void shouldFailForInvalidPhone() {
            NotificationResult result = service.send("0755123456", "Test SMS");

            assertThat(result.status()).isEqualTo(DeliveryStatus.FAILED);
            assertThat(result.detail()).contains("E.164 format");
        }
    }

    @Nested
    @DisplayName("PushNotification")
    class PushNotificationTests {

        private final NotificationService service =
                NotificationServiceFactory.createOrThrow(NotificationChannel.PUSH);

        @Test
        @DisplayName("should deliver to a valid device token")
        void shouldDeliverToValidToken() {
            String validToken = "dGhpcyBpcyBhIGZha2UgZGV2aWNlIHRva2VuITEy";
            NotificationResult result = service.send(validToken, "Your parcel is out for delivery");

            assertThat(result.channel()).isEqualTo(NotificationChannel.PUSH);
            assertThat(result.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("should fail for short or blank device token")
        void shouldFailForInvalidToken() {
            NotificationResult result = service.send("short", "Message");

            assertThat(result.status()).isEqualTo(DeliveryStatus.FAILED);
            assertThat(result.detail()).contains("Invalid FCM device token");
        }
    }
}
