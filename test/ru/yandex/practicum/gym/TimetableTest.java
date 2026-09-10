package ru.yandex.practicum.gym;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimetableTest {

    @Test
    @DisplayName("Получение расписания за день с одной тренировкой")
    void testGetTrainingSessionsForDaySingleSession() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Group group = new Group("Акробатика для детей", Age.CHILD, 60);
        Coach coach = new Coach("Васильев", "Николай", "Сергеевич");
        DayOfWeek day = DayOfWeek.MONDAY;
        TimeOfDay time = new TimeOfDay(13, 0);
        TrainingSession singleTrainingSession = new TrainingSession(group, coach, day, time);

        timetable.addNewTrainingSession(singleTrainingSession);

        // ACT
        Map<TimeOfDay, List<TrainingSession>> mondaySessions = timetable.getTrainingSessionsForDay(day);
        Map<TimeOfDay, List<TrainingSession>> tuesdaySessions = timetable.getTrainingSessionsForDay(DayOfWeek.TUESDAY);

        // ASSERT
        assertEquals(1, mondaySessions.size());
        assertTrue(mondaySessions.containsKey(time));
        assertEquals(1, mondaySessions.get(time).size());
        assertSame(singleTrainingSession, mondaySessions.get(time).getFirst());

        assertNotNull(tuesdaySessions);
        assertTrue(tuesdaySessions.isEmpty());
    }

    @Test
    @DisplayName("Получение и сортировка по времени нескольких тренировок в течение дня")
    void testGetTrainingSessionsForDayMultipleSessions() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Coach coach = new Coach("Васильев", "Николай", "Сергеевич");

        Group groupAdult = new Group("Акробатика для взрослых", Age.ADULT, 90);
        TrainingSession thursdayAdultTrainingSession = new TrainingSession(groupAdult, coach,
                DayOfWeek.THURSDAY, new TimeOfDay(20, 0));

        timetable.addNewTrainingSession(thursdayAdultTrainingSession);

        Group groupChild = new Group("Акробатика для детей", Age.CHILD, 60);
        TrainingSession mondayChildTrainingSession = new TrainingSession(groupChild, coach,
                DayOfWeek.MONDAY, new TimeOfDay(13, 0));
        TrainingSession thursdayChildTrainingSession = new TrainingSession(groupChild, coach,
                DayOfWeek.THURSDAY, new TimeOfDay(13, 0));
        TrainingSession saturdayChildTrainingSession = new TrainingSession(groupChild, coach,
                DayOfWeek.SATURDAY, new TimeOfDay(10, 0));

        timetable.addNewTrainingSession(mondayChildTrainingSession);
        timetable.addNewTrainingSession(thursdayChildTrainingSession);
        timetable.addNewTrainingSession(saturdayChildTrainingSession);

        // ACT
        Map<TimeOfDay, List<TrainingSession>> mondaySessions = timetable.getTrainingSessionsForDay(DayOfWeek.MONDAY);
        Map<TimeOfDay, List<TrainingSession>> thursdaySessions =
                timetable.getTrainingSessionsForDay(DayOfWeek.THURSDAY);
        Map<TimeOfDay, List<TrainingSession>> tuesdaySessions = timetable.getTrainingSessionsForDay(DayOfWeek.TUESDAY);

        // ASSERT
        assertEquals(1, mondaySessions.size());
        assertEquals(1, mondaySessions.get(new TimeOfDay(13, 0)).size());

        assertEquals(2, thursdaySessions.size());
        List<TimeOfDay> sortedTimes = new ArrayList<>(thursdaySessions.keySet());
        assertEquals(new TimeOfDay(13, 0), sortedTimes.getFirst());
        assertEquals(new TimeOfDay(20, 0), sortedTimes.get(1));

        assertTrue(tuesdaySessions.isEmpty());
    }

    @Test
    @DisplayName("Поиск тренировок по конкретному дню и времени начала")
    void testGetTrainingSessionsForDayAndTime() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Group group = new Group("Акробатика для детей", Age.CHILD, 60);
        Coach coach = new Coach("Васильев", "Николай", "Сергеевич");
        DayOfWeek day = DayOfWeek.MONDAY;
        TimeOfDay time = new TimeOfDay(13, 0);
        TrainingSession singleTrainingSession = new TrainingSession(group, coach, day, time);

        timetable.addNewTrainingSession(singleTrainingSession);

        // ACT
        List<TrainingSession> monday13Sessions = timetable.getTrainingSessionsForDayAndTime(day, time);
        List<TrainingSession> monday14Sessions = timetable.getTrainingSessionsForDayAndTime(
                day, new TimeOfDay(14, 0)
        );

        // ASSERT
        assertEquals(1, monday13Sessions.size());
        assertSame(singleTrainingSession, monday13Sessions.getFirst());
        assertNotNull(monday14Sessions);
        assertTrue(monday14Sessions.isEmpty());
    }

    @Test
    @DisplayName("Параллельные занятия разных групп в одно и то же время")
    void testMultipleSessionsAtSameTimeAndDay() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Coach coach1 = new Coach("Васильев", "Николай", "Сергеевич");
        Coach coach2 = new Coach("Измайлов", "Владимир", "Владимирович");

        Group group1 = new Group("Акробатика для детей", Age.CHILD, 60);
        Group group2 = new Group("Растяжка для взрослых", Age.ADULT, 60);

        DayOfWeek day = DayOfWeek.WEDNESDAY;

        // создаем разные экземпляры времени, чтобы заодно проверить equals и hashCode
        TimeOfDay time1 = new TimeOfDay(18, 0);
        TimeOfDay time2 = new TimeOfDay(18, 0);

        TrainingSession session1 = new TrainingSession(group1, coach1, day, time1);
        TrainingSession session2 = new TrainingSession(group2, coach2, day, time2);

        timetable.addNewTrainingSession(session1);
        timetable.addNewTrainingSession(session2);

        // ACT
        List<TrainingSession> sessionsAt18 = timetable.getTrainingSessionsForDayAndTime(
                day, new TimeOfDay(18, 0)
        );
        Map<TimeOfDay, List<TrainingSession>> wednesdaySessions = timetable.getTrainingSessionsForDay(day);

        // ASSERT
        assertEquals(1, wednesdaySessions.size());
        assertEquals(2, sessionsAt18.size());
        assertTrue(sessionsAt18.contains(session1));
        assertTrue(sessionsAt18.contains(session2));
    }

    @Test
    @DisplayName("Сортировка времени с одинаковым часом, но разными минутами")
    void testTimeOfDayNaturalOrderingWithMinutes() {
        // ARRANGE
        Timetable timetable = new Timetable();
        Coach coach = new Coach("Семенов", "Владимир", "Константинович");
        Group group = new Group("Общая гимнастика", Age.CHILD, 120);

        TimeOfDay timeLate = new TimeOfDay(10, 45);
        TimeOfDay timeEarly = new TimeOfDay(10, 15);

        TrainingSession sessionLate = new TrainingSession(group, coach, DayOfWeek.FRIDAY, timeLate);
        TrainingSession sessionEarly = new TrainingSession(group, coach, DayOfWeek.FRIDAY, timeEarly);

        // специально добавляем в обратном порядке
        timetable.addNewTrainingSession(sessionLate);
        timetable.addNewTrainingSession(sessionEarly);

        // ACT
        Map<TimeOfDay, List<TrainingSession>> fridaySessions = timetable.getTrainingSessionsForDay(DayOfWeek.FRIDAY);

        // ASSERT
        List<TimeOfDay> keys = new ArrayList<>(fridaySessions.keySet());
        assertEquals(timeEarly, keys.getFirst());
        assertEquals(timeLate, keys.get(1));
    }

    @Test
    @DisplayName("Корректная привязка разных тренеров к одной и той же группе в разные дни")
    void testSameGroupDifferentCoachesAtDifferentTimes() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Group acrobatics = new Group("Акробатика для детей 10-12 лет", Age.CHILD, 60);
        Coach firstCoach = new Coach("Семенов", "Владимир", "Владимирович");
        Coach secondCoach = new Coach("Семенов", "Владимир", "Константинович");

        TimeOfDay time = new TimeOfDay(18, 0);

        TrainingSession mondaySession = new TrainingSession(acrobatics, firstCoach, DayOfWeek.MONDAY, time);
        TrainingSession fridaySession = new TrainingSession(acrobatics, secondCoach, DayOfWeek.FRIDAY, time);

        timetable.addNewTrainingSession(mondaySession);
        timetable.addNewTrainingSession(fridaySession);

        // ACT
        List<TrainingSession> mondaySessions = timetable.getTrainingSessionsForDayAndTime(DayOfWeek.MONDAY, time);
        List<TrainingSession> fridaySessions = timetable.getTrainingSessionsForDayAndTime(DayOfWeek.FRIDAY, time);

        // ASSERT
        assertEquals(1, mondaySessions.size());
        assertSame(firstCoach, mondaySessions.getFirst().getCoach());
        assertSame(acrobatics, mondaySessions.getFirst().getGroup());

        assertEquals(1, fridaySessions.size());
        assertSame(secondCoach, fridaySessions.getFirst().getCoach());
        assertSame(acrobatics, fridaySessions.getFirst().getGroup());
    }

    @Test
    @DisplayName("Запросы к пустому расписанию возвращают пустые коллекции")
    void testEmptyTimetableQueriesDoNotThrowException() {
        // ARRANGE
        Timetable timetable = new Timetable();
        TimeOfDay time = new TimeOfDay(12, 0);

        // ACT & ASSERT
        for (DayOfWeek day : DayOfWeek.values()) {
            Map<TimeOfDay, List<TrainingSession>> dayResult = timetable.getTrainingSessionsForDay(day);
            assertNotNull(dayResult, "Мапа занятий не должна быть null для дня: " + day);
            assertTrue(dayResult.isEmpty(), "Расписание должно быть пустым для дня: " + day);

            List<TrainingSession> timeResult = timetable.getTrainingSessionsForDayAndTime(day, time);
            assertNotNull(timeResult, "Список занятий не должен быть null для дня: " + day);
            assertTrue(timeResult.isEmpty(), "Список занятий должен быть пустым для дня: " + day);
        }
    }

    @Test
    @DisplayName("Полученные коллекции с тренировками нельзя модифицировать")
    void testReturnedCollectionsAreUnmodifiable() {
        // ARRANGE
        Timetable timetable = new Timetable();
        Group group = new Group("Акробатика", Age.CHILD, 60);
        Coach coach = new Coach("Васильев", "Николай", "Сергеевич");
        DayOfWeek day = DayOfWeek.MONDAY;
        TimeOfDay time = new TimeOfDay(10, 0);

        timetable.addNewTrainingSession(new TrainingSession(group, coach, day, time));

        // ACT
        Map<TimeOfDay, List<TrainingSession>> daySessions = timetable.getTrainingSessionsForDay(day);
        List<TrainingSession> sessionsAtTime = timetable.getTrainingSessionsForDayAndTime(day, time);

        // ASSERT
        assertThrows(UnsupportedOperationException.class, () -> daySessions.remove(time));
        assertThrows(UnsupportedOperationException.class, sessionsAtTime::clear);
    }

    @Test
    @DisplayName("Подсчет тренировок тренеров: возвращается список по убыванию количества занятий")
    void testGetCountByCoachesSortedDescending() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Coach coach1 = new Coach("Васильев", "Николай", "Сергеевич");
        Coach coach2 = new Coach("Семенов", "Владимир", "Константинович");
        Coach coach3 = new Coach("Измайлов", "Владимир", "Владимирович");

        Group group = new Group("Акробатика", Age.ADULT, 60);

        TimeOfDay time = new TimeOfDay(10, 0);

        timetable.addNewTrainingSession(new TrainingSession(group, coach1, DayOfWeek.MONDAY, time));

        timetable.addNewTrainingSession(new TrainingSession(group, coach2, DayOfWeek.TUESDAY, time));
        timetable.addNewTrainingSession(new TrainingSession(group, coach2, DayOfWeek.WEDNESDAY, time));

        timetable.addNewTrainingSession(new TrainingSession(group, coach3, DayOfWeek.THURSDAY, time));
        timetable.addNewTrainingSession(new TrainingSession(group, coach3, DayOfWeek.FRIDAY, time));
        timetable.addNewTrainingSession(new TrainingSession(group, coach3, DayOfWeek.SATURDAY, time));

        // ACT
        List<Map.Entry<Coach, Integer>> result = timetable.getCountByCoaches();

        // ASSERT
        assertEquals(3, result.size());
        assertEquals(coach3, result.getFirst().getKey());
        assertEquals(3, result.getFirst().getValue());
        assertEquals(coach2, result.get(1).getKey());
        assertEquals(2, result.get(1).getValue());
        assertEquals(coach1, result.get(2).getKey());
        assertEquals(1, result.get(2).getValue());
    }

    @Test
    @DisplayName("Подсчет тренировок тренеров: одинаковое количество занятий у нескольких тренеров")
    void testGetCountByCoachesWithEqualCounts() {
        // ARRANGE
        Timetable timetable = new Timetable();

        Coach coachA = new Coach("Алексеев", "Алексей", "Алексеевич");
        Coach coachB = new Coach("Борисов", "Борис", "Борисович");
        Group group = new Group("Гимнастика", Age.CHILD, 60);
        TimeOfDay time = new TimeOfDay(10, 0);

        timetable.addNewTrainingSession(new TrainingSession(group, coachA, DayOfWeek.MONDAY, time));
        timetable.addNewTrainingSession(new TrainingSession(group, coachA, DayOfWeek.TUESDAY, time));

        timetable.addNewTrainingSession(new TrainingSession(group, coachB, DayOfWeek.WEDNESDAY, time));
        timetable.addNewTrainingSession(new TrainingSession(group, coachB, DayOfWeek.THURSDAY, time));

        // ACT
        List<Map.Entry<Coach, Integer>> result = timetable.getCountByCoaches();

        // ASSERT
        assertEquals(2, result.size());
        assertEquals(2, result.getFirst().getValue());
        assertEquals(2, result.get(1).getValue());
        assertTrue(result.stream().anyMatch(e -> e.getKey().equals(coachA)));
        assertTrue(result.stream().anyMatch(e -> e.getKey().equals(coachB)));
    }

    @Test
    @DisplayName("Подсчет тренировок тренеров: корректно распознает одинаковых тренеров из разных объектов по equals")
    void testGetCountByCoachesDifferentObjectInstances() {
        // ARRANGE
        Timetable timetable = new Timetable();
        Group group = new Group("Растяжка", Age.ADULT, 60);
        TimeOfDay time = new TimeOfDay(10, 0);

        Coach coachInstance1 = new Coach("Измайлов", "Владимир", "Владимирович");
        Coach coachInstance2 = new Coach("Измайлов", "Владимир", "Владимирович");

        timetable.addNewTrainingSession(new TrainingSession(group, coachInstance1, DayOfWeek.MONDAY, time));
        timetable.addNewTrainingSession(new TrainingSession(group, coachInstance2, DayOfWeek.FRIDAY, time));

        // ACT
        List<Map.Entry<Coach, Integer>> result = timetable.getCountByCoaches();

        // ASSERT
        assertEquals(1, result.size());
        assertEquals(coachInstance1, result.getFirst().getKey());
        assertEquals(2, result.getFirst().getValue());
    }

    @Test
    @DisplayName("Подсчет тренировок тренеров: пустой список при отсутствии тренировок")
    void testGetCountByCoachesEmptyTimetable() {
        // ARRANGE
        Timetable timetable = new Timetable();

        // ACT
        List<Map.Entry<Coach, Integer>> result = timetable.getCountByCoaches();

        // ASSERT
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Подсчет тренировок тренеров: защита возвращаемого списка от изменений")
    void testGetCountByCoachesReturnsUnmodifiableList() {
        // ARRANGE
        Timetable timetable = new Timetable();
        Coach coach = new Coach("Васильев", "Николай", "Сергеевич");
        Group group = new Group("Акробатика", Age.CHILD, 60);
        TimeOfDay time = new TimeOfDay(10, 0);

        timetable.addNewTrainingSession(new TrainingSession(group, coach, DayOfWeek.MONDAY, time));

        // ACT
        List<Map.Entry<Coach, Integer>> result = timetable.getCountByCoaches();

        // ASSERT
        assertThrows(UnsupportedOperationException.class, result::clear);
    }
}
