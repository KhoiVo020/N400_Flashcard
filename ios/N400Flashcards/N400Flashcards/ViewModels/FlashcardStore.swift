import Foundation

@MainActor
final class FlashcardStore: ObservableObject {
    @Published private(set) var allFlashcards: [Flashcard] = []
    @Published private(set) var flashcards: [Flashcard] = []
    @Published private(set) var selectedWords: Set<String> = []
    @Published private(set) var currentIndex = 0
    @Published private(set) var isShuffled = false
    @Published private(set) var ttsSpeed: Float = 0.85

    var currentCard: Flashcard? {
        guard flashcards.indices.contains(currentIndex) else { return nil }
        return flashcards[currentIndex]
    }

    init() {
        loadFlashcards()
    }

    func nextCard() {
        guard !flashcards.isEmpty else { return }
        currentIndex = (currentIndex + 1) % flashcards.count
    }

    func previousCard() {
        guard !flashcards.isEmpty else { return }
        currentIndex = currentIndex > 0 ? currentIndex - 1 : flashcards.count - 1
    }

    func toggleShuffle() {
        isShuffled.toggle()
        flashcards = buildDeck(selectedWords: selectedWords, isShuffled: isShuffled)
        currentIndex = 0
    }

    func toggleWordSelection(_ word: String) {
        if selectedWords.contains(word) {
            selectedWords.remove(word)
        } else {
            selectedWords.insert(word)
        }
        flashcards = buildDeck(selectedWords: selectedWords, isShuffled: isShuffled)
        currentIndex = 0
    }

    func selectAllWords() {
        selectedWords = Set(allFlashcards.map(\.word))
        flashcards = buildDeck(selectedWords: selectedWords, isShuffled: isShuffled)
        currentIndex = 0
    }

    func clearSelectedWords() {
        selectedWords = []
        flashcards = []
        currentIndex = 0
    }

    func toggleSpeed() {
        switch ttsSpeed {
        case 0.5:
            ttsSpeed = 0.85
        case 0.85:
            ttsSpeed = 1.1
        default:
            ttsSpeed = 0.5
        }
    }

    private func loadFlashcards() {
        guard let url = Bundle.main.url(forResource: "Flashcards", withExtension: "json") else {
            return
        }

        do {
            let data = try Data(contentsOf: url)
            let decoded = try JSONDecoder().decode([Flashcard].self, from: data)
            let sorted = decoded.sorted { $0.word.localizedCaseInsensitiveCompare($1.word) == .orderedAscending }
            allFlashcards = sorted
            selectedWords = Set(sorted.map(\.word))
            flashcards = sorted
        } catch {
            print("Failed to load flashcards: \(error)")
        }
    }

    private func buildDeck(selectedWords: Set<String>, isShuffled: Bool) -> [Flashcard] {
        let selectedCards = allFlashcards.filter { selectedWords.contains($0.word) }
        return isShuffled ? selectedCards.shuffled() : selectedCards
    }
}
