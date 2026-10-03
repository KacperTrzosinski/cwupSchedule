package pl.legnica.planzajec.data.repository

import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.parser.model.ChangeFlag
import pl.legnica.planzajec.parser.model.Lesson

class ChangeDetector {

    data class DiffResult(
        val updatedEntities: List<LessonEntity>,
        val changeCount: Int
    )

    /**
     * Compares newly fetched lessons with previously cached entities for a group.
     * Computes change flags (NEW, CHANGED_ROOM_OR_TIME, CANCELLED, NONE).
     */
    fun detectChanges(
        newLessons: List<Lesson>,
        existingEntities: List<LessonEntity>,
        groupCode: String
    ): DiffResult {
        if (existingEntities.isEmpty()) {
            // First download, no changes to highlight
            val entities = newLessons.map { LessonEntity.fromDomain(it, groupCode, ChangeFlag.NONE) }
            return DiffResult(entities, changeCount = 0)
        }

        var changes = 0
        val matchedOldEntities = mutableSetOf<LessonEntity>()
        val resultEntities = mutableListOf<LessonEntity>()

        for (newLesson in newLessons) {
            // Exact match
            val exactMatch = existingEntities.find { old ->
                !matchedOldEntities.contains(old) &&
                    old.date == newLesson.date &&
                    old.startTime == newLesson.startTime &&
                    old.endTime == newLesson.endTime &&
                    old.subjectShort == newLesson.subjectShort &&
                    old.subgroup == newLesson.subgroup &&
                    old.room == newLesson.room
            }

            if (exactMatch != null) {
                matchedOldEntities.add(exactMatch)
                resultEntities.add(LessonEntity.fromDomain(newLesson, groupCode, ChangeFlag.NONE))
                continue
            }

            // Room or time change match (same subject, date and subgroup)
            val partialMatch = existingEntities.find { old ->
                !matchedOldEntities.contains(old) &&
                    old.date == newLesson.date &&
                    old.subjectShort == newLesson.subjectShort &&
                    old.subgroup == newLesson.subgroup
            }

            if (partialMatch != null) {
                matchedOldEntities.add(partialMatch)
                changes++
                val note = buildString {
                    if (partialMatch.room != newLesson.room) {
                        append("Zmieniono salę z ${partialMatch.room} na ${newLesson.room}. ")
                    }
                    if (partialMatch.startTime != newLesson.startTime || partialMatch.endTime != newLesson.endTime) {
                        append("Zmieniono godz. z ${partialMatch.startTime}-${partialMatch.endTime} na ${newLesson.startTime}-${newLesson.endTime}.")
                    }
                }.trim()

                resultEntities.add(
                    LessonEntity.fromDomain(
                        lesson = newLesson,
                        groupCode = groupCode,
                        changeFlag = ChangeFlag.CHANGED_ROOM_OR_TIME,
                        note = note
                    )
                )
                continue
            }

            // New lesson
            changes++
            resultEntities.add(LessonEntity.fromDomain(newLesson, groupCode, ChangeFlag.NEW))
        }

        // Check for cancelled lessons (existed before in this date range, but missing now)
        val newDates = newLessons.map { it.date }.toSet()
        val cancelledEntities = existingEntities.filter { old ->
            !matchedOldEntities.contains(old) && newDates.contains(old.date)
        }

        for (cancelled in cancelledEntities) {
            changes++
            resultEntities.add(
                cancelled.copy(
                    id = 0, // Generate new row
                    changeFlag = ChangeFlag.CANCELLED,
                    previousRoomOrTimeNote = "Zajęcia odwołane"
                )
            )
        }

        return DiffResult(resultEntities, changes)
    }
}
