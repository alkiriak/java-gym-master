package ru.yandex.practicum.gym;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.HashMap;

public class Timetable {

    private final Map<DayOfWeek, NavigableMap<TimeOfDay, List<TrainingSession>>> timetable = new HashMap<>();
    private final FrequencyCounter<Coach> coachFrequencyCounter = new FrequencyCounter<>();

    /**
     * Добавляет тренировочную сессию в расписание.
     * Сложность: O(log M) вставка в TreeMap слота дня + O(1) инкремент счетчика тренера.
     */
    public void addNewTrainingSession(TrainingSession trainingSession) {
        DayOfWeek sessionDay = trainingSession.getDayOfWeek();
        TimeOfDay sessionTime = trainingSession.getTimeOfDay();

        timetable
                .computeIfAbsent(sessionDay, day -> new TreeMap<>())
                .computeIfAbsent(sessionTime, time -> new ArrayList<>())
                .add(trainingSession);

        coachFrequencyCounter.increment(trainingSession.getCoach());
    }

    /**
     * Возвращает все тренировки за конкретный день, отсортированные по времени начала.
     * Сложность: O(1).
     */
    public Map<TimeOfDay, List<TrainingSession>> getTrainingSessionsForDay(DayOfWeek dayOfWeek) {
        NavigableMap<TimeOfDay, List<TrainingSession>> sessions = timetable.get(dayOfWeek);
        if (sessions == null) {
            return Collections.emptyNavigableMap();
        }
        return Collections.unmodifiableNavigableMap(sessions);
    }

    /**
     * Возвращает все тренировки за конкретный день и конкретное время начала.
     * Сложность: O(log M) для поиска слота в TreeMap.
     */
    public List<TrainingSession> getTrainingSessionsForDayAndTime(DayOfWeek dayOfWeek, TimeOfDay timeOfDay) {
        Map<TimeOfDay, List<TrainingSession>> sessions = timetable.get(dayOfWeek);
        if (sessions == null) {
            return Collections.emptyList();
        }
        List<TrainingSession> sessionsAtTime = sessions.get(timeOfDay);
        if (sessionsAtTime == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(sessionsAtTime);
    }

    /**
     * Возвращает список тренеров с количеством занятий, отсортированный по убыванию числа занятий.
     * Сложность: O(K), где K - число уникальных тренеров.
     */
    public List<Map.Entry<Coach, Integer>> getCountByCoaches() {
        return coachFrequencyCounter.getSortedEntries();
    }
}
