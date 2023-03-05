package ru.netology.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;

public class CardTest {
    private String planningDate;

    public String generateDate(long addDays, String pattern) {
        return LocalDate.now().plusDays(addDays).format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    @Test
    public void shouldSubmitRequest() {
        planningDate = generateDate(4, "dd.MM.yyyy");

        open("http://localhost:7777");
        SelenideElement form = $("form");
        form.$("[data-test-id='city'] input").setValue("Киров");
        $("[data-test-id='date'] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        form.$("[data-test-id='date'] input").setValue(planningDate);
        form.$("[data-test-id='name'] input").setValue("Василий");
        form.$("[data-test-id='phone'] input").setValue("+79270000000");
        form.$("[data-test-id='agreement']").click();
        form.$(byText("Забронировать")).click();

        $(withText("Успешно!")).shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content")
                .shouldHave(Condition.text("Встреча успешно забронирована на " + planningDate), Duration.ofSeconds(15))
                .shouldBe(Condition.visible);
    }

    @Test
    public void shouldSetCityFromListAndDateFromCalendar() {
        open("http://localhost:7777");

        // Ввод двух букв в поле «Город», после чего выбор нужного города из выпадающего списка
        $("[data-test-id='city'] input").sendKeys("Ки");
        $$(".popup_visible .menu-item").findBy(text("Киров")).click();

        // Выбор даты на неделю вперёд, начиная от текущей даты, через инструмент календаря
        int addDays = 7;
        if (generateDate(addDays, "E").equals("Sat")) {
            addDays += 2;                                                 // если попали на сб., то перенос на пн.
        } else if (generateDate(addDays, "E").equals("Sun")) {
            addDays++;                                                    // если попали на вск., то перенос на пн.
        }
        // если месяцы выбранной в календаре по умолчанию даты и планируемой даты не равны
        planningDate = generateDate(addDays, "MM");
        $("[data-test-id='date'] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        if (!generateDate(addDays, "MM").equals(generateDate(3, "MM"))) {
            // то перелистываем страницу
            $("[data-step='1']").click();
        }
        // используем поиск по коллекции элементов для нахождения даты в календаре
        $$(".popup_visible .calendar__day").findBy(text(generateDate(addDays, "d"))).click();

        $("[data-test-id='name'] input").setValue("Василий");
        $("[data-test-id='phone'] input").setValue("+79270000000");
        $("[data-test-id='agreement']").click();
        $(byText("Забронировать")).click();

        planningDate = generateDate(addDays, "dd.MM.yyyy");

        $(withText("Успешно!")).shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content")
                .shouldHave(Condition.text("Встреча успешно забронирована на " + planningDate), Duration.ofSeconds(15))
                .shouldBe(Condition.visible);
    }
}


