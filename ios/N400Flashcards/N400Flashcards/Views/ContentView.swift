import SwiftUI
import UIKit

struct ContentView: View {
    @EnvironmentObject private var store: FlashcardStore
    @EnvironmentObject private var speechManager: SpeechManager

    @State private var isWordPickerPresented = false

    var body: some View {
        GeometryReader { geometry in
            let isLandscape = geometry.size.width > geometry.size.height
            let isCompactHeight = geometry.size.height < 760 || isLandscape

            ZStack {
                LinearGradient(
                    colors: [Color(red: 224 / 255, green: 247 / 255, blue: 250 / 255),
                             Color(red: 178 / 255, green: 235 / 255, blue: 242 / 255)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()

                if isLandscape {
                    HStack(spacing: 12) {
                        ControlPane(
                            isCompactHeight: true,
                            isLandscape: true,
                            isWordPickerPresented: $isWordPickerPresented
                        )
                        .frame(minWidth: 240, idealWidth: 280, maxWidth: 320)

                        DeckPane(isCompactHeight: true, isLandscape: true, isWordPickerPresented: $isWordPickerPresented)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                    .padding(12)
                } else {
                    VStack(spacing: 0) {
                        ControlPane(
                            isCompactHeight: isCompactHeight,
                            isLandscape: false,
                            isWordPickerPresented: $isWordPickerPresented
                        )
                        DeckPane(
                            isCompactHeight: isCompactHeight,
                            isLandscape: false,
                            isWordPickerPresented: $isWordPickerPresented
                        )
                    }
                }
            }
            .sheet(isPresented: $isWordPickerPresented) {
                WordSelectionSheet()
                    .environmentObject(store)
            }
            .onAppear {
                speechManager.refreshVoices()
            }
        }
    }
}

private struct DeckPane: View {
    @EnvironmentObject private var store: FlashcardStore

    let isCompactHeight: Bool
    let isLandscape: Bool
    @Binding var isWordPickerPresented: Bool

    var body: some View {
        VStack(spacing: 0) {
            Group {
                if let card = store.currentCard {
                    FlashcardView(card: card, isCompactHeight: isCompactHeight, isLandscape: isLandscape)
                } else {
                    EmptyDeckView(isWordPickerPresented: $isWordPickerPresented)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            BottomNavBar(isCompactHeight: isCompactHeight)
        }
    }
}

private struct ControlPane: View {
    @EnvironmentObject private var store: FlashcardStore
    @EnvironmentObject private var speechManager: SpeechManager

    let isCompactHeight: Bool
    let isLandscape: Bool
    @Binding var isWordPickerPresented: Bool

    private var logoSize: CGFloat { isCompactHeight ? 56 : 80 }
    private var verticalPadding: CGFloat { isCompactHeight ? 12 : 16 }
    private var sectionSpacing: CGFloat { isCompactHeight ? 6 : 8 }

    var body: some View {
        let content = VStack(spacing: sectionSpacing) {
            BundleImage(name: "n400_app_logo", ext: "png", subdirectory: nil)
                .scaledToFit()
                .frame(width: logoSize, height: logoSize)

            Text("N400 WhatMean Flashcard 2026")
                .font(isCompactHeight ? .title3.bold() : .title2.bold())
                .foregroundStyle(Color(red: 2 / 255, green: 119 / 255, blue: 189 / 255))
                .multilineTextAlignment(.center)

            if isLandscape {
                actionButton(title: speedLabel) {
                    store.toggleSpeed()
                }
                actionButton(title: voiceLabel) {
                    speechManager.toggleVoicePreference()
                }
                .disabled(!speechManager.hasEnhancedUSVoice)
                actionButton(title: store.isShuffled ? "Unshuffle" : "Shuffle", systemImage: "shuffle") {
                    store.toggleShuffle()
                }
            } else {
                HStack(spacing: 8) {
                    actionButton(title: speedLabel) {
                        store.toggleSpeed()
                    }
                    actionButton(title: voiceLabel) {
                        speechManager.toggleVoicePreference()
                    }
                    .disabled(!speechManager.hasEnhancedUSVoice)
                }

                HStack {
                    Spacer()
                    actionButton(title: store.isShuffled ? "Unshuffle" : "Shuffle", systemImage: "shuffle") {
                        store.toggleShuffle()
                    }
                    Spacer()
                }
            }

            actionButton(title: "Words: \(store.selectedWords.count) / \(store.allFlashcards.count)") {
                isWordPickerPresented = true
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, verticalPadding)
        .frame(maxWidth: .infinity, alignment: .top)

        if isLandscape {
            ScrollView { content }
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
        } else {
            content
        }
    }

    private var speedLabel: String {
        switch store.ttsSpeed {
        case 0.5:
            return "Speed: 0.5"
        case 1.1:
            return "Speed: 1.1"
        default:
            return "Speed: Normal"
        }
    }

    private var voiceLabel: String {
        if speechManager.hasEnhancedUSVoice {
            return "Voice: \(speechManager.preferEnhancedVoice ? "Enhanced US" : "Default")"
        }
        return "Voice: Default"
    }

    @ViewBuilder
    private func actionButton(title: String, systemImage: String? = nil, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 6) {
                if let systemImage {
                    Image(systemName: systemImage)
                }
                Text(title)
                    .frame(maxWidth: .infinity)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 10)
            .padding(.horizontal, 12)
            .background(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(.white.opacity(0.9))
            )
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(Color.black.opacity(0.08), lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
}

private struct WordSelectionSheet: View {
    @EnvironmentObject private var store: FlashcardStore
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("\(store.selectedWords.count) of \(store.allFlashcards.count) selected")
                        .foregroundStyle(.secondary)

                    HStack {
                        Button("Select All") {
                            store.selectAllWords()
                        }
                        Spacer()
                        Button("Clear") {
                            store.clearSelectedWords()
                        }
                    }
                }

                Section {
                    ForEach(store.allFlashcards) { card in
                        Button {
                            store.toggleWordSelection(card.word)
                        } label: {
                            HStack(alignment: .top, spacing: 12) {
                                Image(systemName: store.selectedWords.contains(card.word) ? "checkmark.square.fill" : "square")
                                    .foregroundStyle(store.selectedWords.contains(card.word) ? Color.accentColor : .secondary)
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(card.word)
                                        .font(.body.weight(.medium))
                                        .foregroundStyle(.primary)
                                    Text(card.meaning)
                                        .font(.footnote)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .navigationTitle("Choose Words")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

private struct EmptyDeckView: View {
    @Binding var isWordPickerPresented: Bool

    var body: some View {
        VStack(spacing: 12) {
            Text("No words selected")
                .font(.title3.bold())
                .foregroundStyle(Color(red: 2 / 255, green: 119 / 255, blue: 189 / 255))
            Text("Choose words to start practicing.")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            Button("Choose Words") {
                isWordPickerPresented = true
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct FlashcardView: View {
    @EnvironmentObject private var speechManager: SpeechManager

    let card: Flashcard
    let isCompactHeight: Bool
    let isLandscape: Bool

    @State private var isShowingBack = false

    var body: some View {
        GeometryReader { geometry in
            let aspectRatio: CGFloat = isLandscape ? 0.92 : 0.7
            let widthFromScreen = geometry.size.width * (isLandscape ? 0.78 : (isCompactHeight ? 0.84 : 0.88))
            let widthFromHeight = geometry.size.height * aspectRatio * (isCompactHeight ? 0.9 : 0.94)
            let cardWidth = min(widthFromScreen, widthFromHeight)

            ZStack {
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(.white)
                    .shadow(color: .black.opacity(0.12), radius: 12, y: 4)

                Group {
                    if isShowingBack {
                        FlashcardBack(card: card, isCompactHeight: isCompactHeight, isLandscape: isLandscape)
                            .rotation3DEffect(.degrees(180), axis: (x: 0, y: 1, z: 0))
                            .transition(.opacity)
                    } else {
                        FlashcardFront(card: card, isCompactHeight: isCompactHeight, isLandscape: isLandscape)
                            .transition(.opacity)
                    }
                }
            }
            .frame(width: cardWidth, height: cardWidth / aspectRatio)
            .rotation3DEffect(.degrees(isShowingBack ? 180 : 0), axis: (x: 0, y: 1, z: 0))
            .animation(.easeInOut(duration: 0.5), value: isShowingBack)
            .contentShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .onTapGesture {
                isShowingBack.toggle()
            }
            .position(x: geometry.size.width / 2, y: geometry.size.height / 2)
            .onChange(of: card.id) { _ in
                isShowingBack = false
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, isCompactHeight ? 8 : 16)
    }
}

private struct FlashcardFront: View {
    @EnvironmentObject private var speechManager: SpeechManager
    @EnvironmentObject private var store: FlashcardStore

    let card: Flashcard
    let isCompactHeight: Bool
    let isLandscape: Bool

    var body: some View {
        GeometryReader { geometry in
            let imageHeightRatio: CGFloat = isLandscape ? 0.26 : (isCompactHeight ? 0.34 : 0.40)
            let imageHeight = max(100, min(geometry.size.height * imageHeightRatio, isLandscape ? 150 : 190))

            VStack(spacing: isCompactHeight ? 16 : 24) {
                FlashcardImage(name: card.imageName)
                    .frame(maxWidth: .infinity)
                    .frame(height: imageHeight)

                Text(card.word)
                    .font(titleFont)
                    .fontWeight(.bold)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)

                Spacer(minLength: 0)

                PlayAudioButton(isCompactHeight: isCompactHeight) {
                    speechManager.speak(card.word, speed: store.ttsSpeed)
                }
            }
            .padding(.bottom, isCompactHeight ? 16 : 24)
        }
    }

    private var titleFont: Font {
        if isLandscape {
            return .title3
        }
        if isCompactHeight {
            return .title2
        }
        return .largeTitle
    }
}

private struct FlashcardBack: View {
    @EnvironmentObject private var speechManager: SpeechManager
    @EnvironmentObject private var store: FlashcardStore

    let card: Flashcard
    let isCompactHeight: Bool
    let isLandscape: Bool

    var body: some View {
        VStack(spacing: isCompactHeight ? 24 : 32) {
            Spacer(minLength: 0)

            VStack(spacing: 8) {
                Text("English Meaning")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.secondary)
                Text(card.meaning)
                    .font(isLandscape ? .title3 : .title2)
                    .foregroundStyle(.red)
                    .multilineTextAlignment(.center)
            }

            VStack(spacing: 8) {
                Text("Tiếng Việt")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.secondary)
                Text(card.vietnamese)
                    .font(isLandscape ? .title3 : .title2)
                    .foregroundStyle(.primary)
                    .multilineTextAlignment(.center)
            }

            Spacer(minLength: 0)

            PlayAudioButton(isCompactHeight: isCompactHeight) {
                speechManager.speak(card.meaning, speed: store.ttsSpeed)
            }
        }
        .padding(isCompactHeight ? 20 : 24)
    }
}

private struct FlashcardImage: View {
    let name: String

    var body: some View {
        Group {
            if let uiImage = loadImage() {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFill()
            } else {
                Color.white.opacity(0.7)
                    .overlay(
                        Image(systemName: "photo")
                            .font(.system(size: 44))
                            .foregroundStyle(.secondary)
                    )
            }
        }
        .clipped()
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private func loadImage() -> UIImage? {
        if let url = Bundle.main.url(forResource: name, withExtension: "jpg", subdirectory: "drawable-nodpi"),
           let image = UIImage(contentsOfFile: url.path) {
            return image
        }

        if let url = Bundle.main.url(forResource: "n400_app_logo", withExtension: "png"),
           let image = UIImage(contentsOfFile: url.path) {
            return image
        }

        return nil
    }
}

private struct BundleImage: View {
    let name: String
    let ext: String
    let subdirectory: String?

    var body: some View {
        Group {
            if let uiImage = loadImage() {
                Image(uiImage: uiImage)
                    .resizable()
            } else {
                Color.clear
            }
        }
    }

    private func loadImage() -> UIImage? {
        guard let url = Bundle.main.url(forResource: name, withExtension: ext, subdirectory: subdirectory) else {
            return nil
        }
        return UIImage(contentsOfFile: url.path)
    }
}

private struct PlayAudioButton: View {
    let isCompactHeight: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "play.fill")
                .font(.system(size: isCompactHeight ? 22 : 26, weight: .bold))
                .foregroundStyle(.white)
                .frame(width: isCompactHeight ? 56 : 64, height: isCompactHeight ? 56 : 64)
                .background(Color.accentColor, in: Circle())
        }
        .buttonStyle(.plain)
    }
}

private struct BottomNavBar: View {
    @EnvironmentObject private var store: FlashcardStore

    let isCompactHeight: Bool

    var body: some View {
        HStack {
            Button {
                store.previousCard()
            } label: {
                Image(systemName: "arrow.left")
                    .font(.title2)
                    .frame(width: 44, height: 44)
            }
            .disabled(store.flashcards.isEmpty)

            Spacer()

            Text(store.flashcards.isEmpty ? "0 / 0" : "\(store.currentIndex + 1) / \(store.flashcards.count)")
                .font(.headline)

            Spacer()

            Button {
                store.nextCard()
            } label: {
                Image(systemName: "arrow.right")
                    .font(.title2)
                    .frame(width: 44, height: 44)
            }
            .disabled(store.flashcards.isEmpty)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, isCompactHeight ? 12 : 24)
    }
}
