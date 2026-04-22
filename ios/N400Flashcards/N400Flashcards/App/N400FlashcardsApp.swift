import SwiftUI

@main
struct N400FlashcardsApp: App {
    @StateObject private var store = FlashcardStore()
    @StateObject private var speechManager = SpeechManager()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(store)
                .environmentObject(speechManager)
        }
    }
}
