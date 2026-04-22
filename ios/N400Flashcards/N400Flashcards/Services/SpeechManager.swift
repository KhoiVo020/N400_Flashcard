import AVFoundation
import Foundation

final class SpeechManager: NSObject, ObservableObject {
    @Published private(set) var hasEnhancedUSVoice = false
    @Published var preferEnhancedVoice = false

    private let synthesizer = AVSpeechSynthesizer()
    private var enhancedVoiceIdentifier: String?

    override init() {
        super.init()
        synthesizer.delegate = self
        refreshVoices()
    }

    func refreshVoices() {
        let enhancedVoice = AVSpeechSynthesisVoice.speechVoices().first { voice in
            voice.language == "en-US" && voice.quality == .enhanced
        }
        enhancedVoiceIdentifier = enhancedVoice?.identifier
        hasEnhancedUSVoice = enhancedVoice != nil
        if !hasEnhancedUSVoice {
            preferEnhancedVoice = false
        }
    }

    func toggleVoicePreference() {
        guard hasEnhancedUSVoice else { return }
        preferEnhancedVoice.toggle()
    }

    func speak(_ text: String, speed: Float) {
        guard !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return
        }

        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
        }

        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = preferredVoice()
        utterance.rate = rate(for: speed)
        utterance.pitchMultiplier = 0.9
        synthesizer.speak(utterance)
    }

    private func preferredVoice() -> AVSpeechSynthesisVoice? {
        if preferEnhancedVoice, let identifier = enhancedVoiceIdentifier {
            return AVSpeechSynthesisVoice(identifier: identifier)
        }
        return AVSpeechSynthesisVoice(language: "en-US")
    }

    private func rate(for speed: Float) -> Float {
        switch speed {
        case 0.5:
            return 0.42
        case 1.1:
            return 0.58
        default:
            return 0.50
        }
    }
}

extension SpeechManager: AVSpeechSynthesizerDelegate {}
