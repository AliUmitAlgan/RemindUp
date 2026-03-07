package com.aliumitalgan.remindup.domain.service

import com.aliumitalgan.remindup.domain.model.AiResponseSource
import com.aliumitalgan.remindup.domain.model.SnoozeCoachingResult
import com.aliumitalgan.remindup.domain.model.TaskBreakdownResult

class RuleBasedFallbackService {

    fun generateTaskBreakdown(goal: String): TaskBreakdownResult {
        val normalizedGoal = goal.trim()
        if (normalizedGoal.isEmpty()) {
            return TaskBreakdownResult(
                subtasks = listOf("Hedefini net bir cümle ile yaz."),
                source = AiResponseSource.FALLBACK,
                message = "Boş içerik olduğu için fallback kullanıldı."
            )
        }

        val defaultSteps = mutableListOf(
            "Hedefi netleştir: '$normalizedGoal' için beklenen sonucu yaz.",
            "İlk 25 dakikalık odak bloğunu planla.",
            "Gerekli kaynakları hazırla (notlar, araçlar, dosyalar).",
            "En küçük uygulanabilir adımı tamamla.",
            "Kısa bir kontrol yapıp bir sonraki adımı belirle."
        )

        when {
            normalizedGoal.contains("çalış", ignoreCase = true) ||
                normalizedGoal.contains("study", ignoreCase = true) -> {
                defaultSteps.add(1, "Konuları 3 parçaya ayır ve her parça için mini hedef belirle.")
                defaultSteps.add("Geçmiş sorulardan en az 10 soru çöz ve eksikleri not et.")
            }
            normalizedGoal.contains("spor", ignoreCase = true) ||
                normalizedGoal.contains("fitness", ignoreCase = true) -> {
                defaultSteps.add(1, "Bugün için ısınma + ana egzersiz + soğuma planı çıkar.")
                defaultSteps.add("Antrenman sonrası kısa ilerleme notu tut.")
            }
            normalizedGoal.contains("proje", ignoreCase = true) ||
                normalizedGoal.contains("project", ignoreCase = true) -> {
                defaultSteps.add(1, "MVP kapsamını yaz: şimdi neyi teslim edeceksin?")
                defaultSteps.add("Görevi teknik işleri 30-60 dakikalık parçalara böl.")
            }
        }

        return TaskBreakdownResult(
            subtasks = defaultSteps.distinct().take(7),
            source = AiResponseSource.FALLBACK,
            message = "AI limit nedeniyle fallback plan üretildi."
        )
    }

    fun getSnoozeCoaching(taskTitle: String, snoozeCount: Int): SnoozeCoachingResult {
        val message = if (snoozeCount >= 3) {
            "'$taskTitle' görevi $snoozeCount kez ertelendi. Bunu 10 dakikalık tek bir adıma indirelim mi?"
        } else {
            "Görevi kısa bir başlangıç adımıyla yeniden başlatabilirsin."
        }

        val actions = listOf(
            "10 dakikalık mini görev oluştur",
            "Yarın sabah 09:00'a taşı",
            "Bu haftadan çıkar"
        )

        return SnoozeCoachingResult(
            message = message,
            actions = actions,
            source = AiResponseSource.FALLBACK
        )
    }

    fun rankTasksByEnergy(tasks: List<String>): List<String> {
        val quick = tasks.filter { it.length <= 40 }
        val deep = tasks.filter { it.length > 40 }
        return quick + deep
    }
}
