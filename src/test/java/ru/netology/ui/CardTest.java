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

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;

public class CardTest {
    private String planningDate;

    public String generateDate(int days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    @Test
    void shouldSubmitRequest() {
        planningDate = generateDate(4);

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
    void shouldSetCityFromListAndDateFromCalendar() {
        open("http://localhost:7777");
        SelenideElement form = $("form");

        // Ввод двух букв в поле «Город», после чего выбор нужного города из выпадающего списка
        form.$("[data-test-id='city'] input").setValue("Ки");
        int count = $$("span.menu-item__control").size();
        for (int i = 0; i < count; i++) {
            String tmp = $$("span.menu-item__control").get(i).getText();
            tmp = tmp.substring(0, 2);
            if (tmp.equals($("[data-test-id='city'] input").getValue())) {
                $$("span.menu-item__control").get(i).click();
                break;
            }
        }

        // Выбор даты на неделю вперёд, начиная от текущей даты, через инструмент календаря
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, 7);                      // устнавливаем дату на неделю позже
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {                      // если попали на сб., то перенос на пн.
            calendar.add(Calendar.DAY_OF_MONTH, 2);
        }
        if (calendar.get(Calendar.DAY_OF_WEEK) == 1) {                      //если попали на вск., то перенос на пн.
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        String dd = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));  // наше новое число в текстовом формате

        DateFormat df = new SimpleDateFormat("MMM");                 // формат краткого текстового названия месяца с точкой на конце
        String month = df.format(calendar.getTime());                       // наш новый месяц
        month = month.substring(0, month.length() - 1);                     // убрать точку в конце строки

        $("[data-test-id='date'] input").click();                  // открыть инструмент календаря на странице
        String calendarName = $("div.calendar__title > div.calendar__name").text().toLowerCase();
        while (!calendarName.contains(month)) {
            $x("//div[@class='calendar__arrow calendar__arrow_direction_right']").click(); // если не наш месяц, то переход в календаре на другой месяц
        }                                                                   // выход из цикла, когда в календаре открыта страница нашего месяца
        // теперь надо выбрать в календаре наше новое число
        SelenideElement table = $("table.calendar__layout");
        table.$(byText(dd)).click();                                        // дата через инструмент календаря выбрана
        planningDate = $("[data-test-id='date'] input").getValue();

        form.$("[data-test-id='name'] input").setValue("Василий");
        form.$("[data-test-id='phone'] input").setValue("+79270000000");
        form.$("[data-test-id='agreement']").click();
        form.$(byText("Забронировать")).click();
        $(withText("Успешно!")).shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content")
                .shouldHave(Condition.text("Встреча успешно забронирована на " + planningDate), Duration.ofSeconds(15))
                .shouldBe(Condition.visible);
    }
}


