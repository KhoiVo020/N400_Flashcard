import Foundation

struct Flashcard: Codable, Hashable, Identifiable {
    let word: String
    let meaning: String
    let vietnamese: String
    let imageName: String

    var id: String { word }
}
