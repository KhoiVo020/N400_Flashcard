package com.khoivo.n400whatmeanflashcard2026.ui.flashcard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class Flashcard(
    val word: String,
    val meaning: String,
    val vietnamese: String,
    val imageName: String
)

data class FlashcardUiState(
    val allFlashcards: List<Flashcard> = emptyList(),
    val flashcards: List<Flashcard> = emptyList(),
    val selectedWords: Set<String> = emptySet(),
    val currentIndex: Int = 0,
    val isShuffled: Boolean = false,
    val ttsSpeed: Float = 0.85f,
    val preferOnlineVoice: Boolean = false
)

class FlashcardViewModel : ViewModel() {

    private val originalList = listOf(
        Flashcard("Action", "Things you do.", "Hành động", "flashcard_action"),
        Flashcard("Admission", "Legal entry into the U.S.", "Sự nhập cảnh / Sự được nhận vào", "flashcard_admission"),
        Flashcard("Advocate", "To support or help.", "Ủng hộ / Biện hộ", "flashcard_advocate"),
        Flashcard("Airplane", "Flying vehicle.", "Máy bay", "flashcard_airplane"),
        Flashcard("Alien", "Not a U.S. citizen.", "Người nước ngoài", "flashcard_alien"),
        Flashcard("Alimony", "Money to ex-spouse after divorce.", "Tiền cấp dưỡng sau ly hôn", "flashcard_alimony"),
        Flashcard("Allegiance", "Loyalty to a country.", "Lòng trung thành", "flashcard_allegiance"),
        Flashcard("Alternative Sentencing", "Punishment without jail.", "Hình phạt thay thế (lao động công ích)", "flashcard_alternative_sentencing"),
        Flashcard("Amnesty", "Official pardon for a group.", "Sự ân xá", "flashcard_amnesty"),
        Flashcard("Apply", "To ask for officially.", "Nộp đơn / Xin", "flashcard_apply"),
        Flashcard("Armed Forces", "The U.S. military.", "Lực lượng vũ trang", "flashcard_armed_forces"),
        Flashcard("Arrested", "Taken by police (handcuffed).", "Bị bắt giữ", "flashcard_arrested"),
        Flashcard("Assassination", "Killing a leader or famous person.", "Ám sát", "flashcard_assassination"),
        Flashcard("Assaulting", "Attacking or hitting someone.", "Tấn công / Hành hung", "flashcard_assaulting"),
        Flashcard("Assist", "To help.", "Giúp đỡ / Hỗ trợ", "flashcard_assist"),
        Flashcard("Associated", "Connected to or involved with.", "Có liên quan / Liên kết", "flashcard_associated"),
        Flashcard("Attempted", "Tried to do something.", "Cố gắng (phạm tội)", "flashcard_attempted"),
        Flashcard("Avoid", "To keep away from (the draft).", "Tránh né", "flashcard_avoid"),
        Flashcard("Bad Conduct", "Bad behavior (in the military).", "Hạnh kiểm xấu", "flashcard_bad_conduct"),
        Flashcard("Bear Arms", "To carry a weapon to fight for the U.S.", "Cầm súng chiến đấu bảo vệ tổ quốc", "flashcard_bear_arms"),
        Flashcard("Become", "To start to be.", "Trở thành", "flashcard_become"),
        Flashcard("Ceremony", "A formal event.", "Buổi lễ (tuyên thệ)", "flashcard_ceremony"),
        Flashcard("Character", "Role or quality.", "Tư cách / Tính chất", "flashcard_character"),
        Flashcard("Charged", "Police formally say you broke the law.", "Bị buộc tội", "flashcard_charged"),
        Flashcard("Cited", "Got a ticket (traffic ticket).", "Bị phạt giao thông (giấy phạt)", "flashcard_cited"),
        Flashcard("Citizen", "A member of a country.", "Công dân", "flashcard_citizen"),
        Flashcard("Civilian", "Person not in military.", "Dân sự / Thường dân", "flashcard_civilian"),
        Flashcard("Civilian Direction", "Orders from non-military leaders.", "Sự chỉ đạo dân sự", "flashcard_civilian_direction"),
        Flashcard("Claim", "To say something is true.", "Tự nhận / Tuyên bố", "flashcard_claim"),
        Flashcard("Cleared", "Charges are removed/dropped.", "Được xóa tội", "flashcard_cleared"),
        Flashcard("Clemency", "Mercy or leniency.", "Sự khoan hồng", "flashcard_clemency"),
        Flashcard("Combat", "Fighting in a war.", "Chiến đấu", "flashcard_combat"),
        Flashcard("Commit", "To do something bad or illegal.", "Phạm (tội) / Thực hiện", "flashcard_commit"),
        Flashcard("Communist Party", "Government owns everything. No freedom.", "Đảng Cộng sản", "flashcard_communist_party"),
        Flashcard("Conduct", "Behavior / Actions.", "Hành vi / Cư xử", "flashcard_conduct"),
        Flashcard("Confined", "Kept in a place (like jail).", "Bị giam giữ / Giam cầm", "flashcard_confined"),
        Flashcard("Conscript", "To force someone to join the army.", "Bắt đi lính (nghĩa vụ quân sự)", "flashcard_conscript"),
        Flashcard("Consent", "To agree / Give permission.", "Đồng ý / Chấp thuận", "flashcard_consent"),
        Flashcard("Consider", "To think of as.", "Coi là / Xem là", "flashcard_consider"),
        Flashcard("Conspire", "To plan with others to do something bad.", "Thông đồng / Âm mưu", "flashcard_conspire"),
        Flashcard("Constitution", "The supreme law of the land.", "Hiến pháp", "flashcard_constitution"),
        Flashcard("Controlled Substance", "Illegal drugs.", "Chất cấm / Ma túy", "flashcard_controlled_substance"),
        Flashcard("Convicted", "Found guilty by a judge or court.", "Bị kết án", "flashcard_convicted"),
        Flashcard("Court-Martialed", "Trial by a military court.", "Ra tòa án binh", "flashcard_court_martialed"),
        Flashcard("Crime / Offense", "Breaking the law.", "Tội / Vi phạm pháp luật", "flashcard_crime_offense"),
        Flashcard("Cultivate", "To grow (plants/drugs).", "Trồng trọt", "flashcard_cultivate"),
        Flashcard("Current", "Happening now.", "Hiện tại", "flashcard_current"),
        Flashcard("Damage", "Breaking or destroying things.", "Gây thiệt hại / Phá hủy", "flashcard_damage"),
        Flashcard("Deferred Adjudication", "Judge delays the decision.", "Hoãn tuyên án", "flashcard_deferred_adjudication"),
        Flashcard("Deferred Prosecution", "Legal delay of charges.", "Hoãn truy tố", "flashcard_deferred_prosecution"),
        Flashcard("Dependents", "People you support financially.", "Người phụ thuộc (vợ/chồng, con cái)", "flashcard_dependents"),
        Flashcard("Deploy", "To move soldiers to a place to work/fight.", "Điều quân / Triển khai quân", "flashcard_deploy"),
        Flashcard("Deportation", "Sent back to your home country.", "Trục xuất", "flashcard_deportation"),
        Flashcard("Deserted", "Ran away from the military.", "Đào ngũ", "flashcard_deserted"),
        Flashcard("Destruction", "Damaging something completely.", "Sự phá hủy / Tàn phá", "flashcard_destruction"),
        Flashcard("Detained", "Stopped or held by police.", "Bị giam giữ / Chặn lại", "flashcard_detained"),
        Flashcard("Detention Facility", "A place where people are forced to stay.", "Trại giam giữ", "flashcard_detention_facility"),
        Flashcard("Dictatorship", "Government with absolute power.", "Chế độ độc tài", "flashcard_dictatorship"),
        Flashcard("Direct", "To lead or give orders.", "Chỉ đạo / Ra lệnh", "flashcard_direct"),
        Flashcard("Disability", "A physical or mental problem.", "Khuyết tật", "flashcard_disability"),
        Flashcard("Discharged", "Left the military.", "Giải ngũ", "flashcard_discharged"),
        Flashcard("Disclose", "To tell the truth / To show.", "Khai báo / Tiết lộ", "flashcard_disclose"),
        Flashcard("Dishonorable", "Shameful; not honorable.", "Không danh dự (bị tước danh dự)", "flashcard_dishonorable"),
        Flashcard("Disposition", "The final result of a court case.", "Kết quả giải quyết / Phán quyết", "flashcard_disposition"),
        Flashcard("Distribute", "To give out or sell.", "Phân phối", "flashcard_distribute"),
        Flashcard("Diversion", "A program to avoid jail/prosecution.", "Biện pháp chuyển hướng (tránh ra tòa)", "flashcard_diversion"),
        Flashcard("Documentation", "Official papers.", "Giấy tờ / Tài liệu", "flashcard_documentation"),
        Flashcard("Domestic Violence", "Harming a family member.", "Bạo hành gia đình", "flashcard_domestic_violence"),
        Flashcard("Drafted", "Forced to join the army (by government).", "Bị gọi nhập ngũ (bắt buộc)", "flashcard_drafted"),
        Flashcard("Duty", "A job or responsibility.", "Nhiệm vụ / Bổn phận", "flashcard_duty"),
        Flashcard("Election", "Choosing a leader by voting.", "Cuộc bầu cử", "flashcard_election"),
        Flashcard("Eligible", "Qualified to do something.", "Đủ điều kiện / Hợp lệ", "flashcard_eligible"),
        Flashcard("Encourage", "To give hope or support.", "Khuyến khích / Động viên", "flashcard_encourage"),
        Flashcard("Engage", "To take part in.", "Tham gia / Thực hiện", "flashcard_engage"),
        Flashcard("Enlist", "To sign up for the army (voluntarily).", "Nhập ngũ (tự nguyện)", "flashcard_enlist"),
        Flashcard("Entry", "Going in.", "Sự nhập cảnh / Lối vào", "flashcard_entry"),
        Flashcard("Establishment", "Creating or setting up.", "Sự thành lập / Thiết lập", "flashcard_establishment"),
        Flashcard("Exemption", "You do not have to do something.", "Sự miễn trừ", "flashcard_exemption"),
        Flashcard("Explanation", "Reason or detail.", "Sự giải thích", "flashcard_explanation"),
        Flashcard("Explosive", "A bomb or material that blows up.", "Chất nổ", "flashcard_explosive"),
        Flashcard("Expunged", "Erased from the record.", "Xóa án tích", "flashcard_expunged"),
        Flashcard("Fail", "Not doing something you should do.", "Không làm / Thất bại", "flashcard_fail"),
        Flashcard("False", "Not true.", "Sai sự thật", "flashcard_false"),
        Flashcard("Federal", "National government.", "Liên bang", "flashcard_federal"),
        Flashcard("File (Taxes)", "To send papers to the government.", "Nộp (hồ sơ thuế)", "flashcard_file_taxes"),
        Flashcard("Force", "Using power or violence.", "Vũ lực", "flashcard_force"),
        Flashcard("Foreign", "From another country.", "(Thuộc) Nước ngoài", "flashcard_foreign"),
        Flashcard("Form", "A paper or type.", "Mẫu đơn / Hình thức", "flashcard_form"),
        Flashcard("Fraudulent", "Fake or a lie to trick someone.", "Gian lận / Giả mạo", "flashcard_fraudulent"),
        Flashcard("Gain", "To get.", "Đạt được / Có được", "flashcard_gain"),
        Flashcard("Gambling", "Playing games for money.", "Cờ bạc", "flashcard_gambling"),
        Flashcard("Genocide", "Killing a whole group of people.", "Diệt chủng", "flashcard_genocide"),
        Flashcard("Group", "People together.", "Nhóm", "flashcard_group"),
        Flashcard("Guerrilla Group", "Using weapons to attack the government.", "Nhóm du kích", "flashcard_guerrilla_group"),
        Flashcard("Harm", "To hurt someone.", "Làm hại", "flashcard_harm"),
        Flashcard("Help", "To assist.", "Giúp đỡ", "flashcard_help"),
        Flashcard("Hereditary Title", "A royal name (King, Prince).", "Tước hiệu cha truyền con nối", "flashcard_hereditary_title"),
        Flashcard("Hijacking", "Stealing a plane or vehicle by force.", "Cướp máy bay / phương tiện", "flashcard_hijacking"),
        Flashcard("Honorable", "Good service (in the military).", "Danh dự (loại xuất ngũ tốt)", "flashcard_honorable"),
        Flashcard("Hostilities", "War or fighting.", "Chiến sự / Sự thù địch", "flashcard_hostilities"),
        Flashcard("Illegal", "Against the law.", "Bất hợp pháp", "flashcard_illegal"),
        Flashcard("Immigration Benefit", "Things like Visa, Green Card.", "Quyền lợi di trú", "flashcard_immigration_benefit"),
        Flashcard("Import", "To bring into the country.", "Nhập khẩu / Đưa vào", "flashcard_import"),
        Flashcard("Incited", "Encouraged others to do bad things.", "Xúi giục / Kích động", "flashcard_incited"),
        Flashcard("Include", "To have as part.", "Bao gồm", "flashcard_include"),
        Flashcard("Income", "Money you earn.", "Thu nhập", "flashcard_income"),
        Flashcard("Influence", "Effect of alcohol or drugs.", "Ảnh hưởng (của rượu/thuốc)", "flashcard_influence"),
        Flashcard("Information", "Facts.", "Thông tin", "flashcard_information"),
        Flashcard("Injuring", "Hurting someone physically.", "Gây thương tích", "flashcard_injuring"),
        Flashcard("Injury", "Harm or damage.", "Chấn thương / Tổn hại", "flashcard_injury"),
        Flashcard("Insurgent", "A fighter against the government.", "Quân nổi dậy", "flashcard_insurgent"),
        Flashcard("Intent", "Purpose or plan to do something.", "Ý định", "flashcard_intent"),
        Flashcard("Involved", "Connected to.", "Có liên quan / Dính líu", "flashcard_involved"),
        Flashcard("Jail / Prison", "A place where criminals are kept.", "Nhà tù", "flashcard_jail_prison"),
        Flashcard("Keep", "To hold or not let go.", "Giữ / Giam giữ", "flashcard_keep"),
        Flashcard("Kidnapping", "Taking a person by force.", "Bắt cóc", "flashcard_kidnapping"),
        Flashcard("Killing", "Making someone die.", "Giết người", "flashcard_killing"),
        Flashcard("Labor Camp", "Prison with forced work.", "Trại lao động khổ sai", "flashcard_labor_camp"),
        Flashcard("Law Enforcement", "Police or agencies that enforce laws.", "Cơ quan thực thi pháp luật", "flashcard_law_enforcement"),
        Flashcard("Lawful", "Legal / Allowed by law.", "Hợp pháp", "flashcard_lawful"),
        Flashcard("Lie", "To say something that is not true.", "Nói dối", "flashcard_lie"),
        Flashcard("Manufacture", "To make or produce.", "Sản xuất / Chế tạo", "flashcard_manufacture"),
        Flashcard("Member", "A person who belongs to a group.", "Thành viên", "flashcard_member"),
        Flashcard("Mental Impairment", "A problem with the mind.", "Suy yếu tâm thần", "flashcard_mental_impairment"),
        Flashcard("Military Unit", "Official army group.", "Đơn vị quân đội", "flashcard_military_unit"),
        Flashcard("Militia", "An army of citizens.", "Dân quân", "flashcard_militia"),
        Flashcard("Misleading", "Making people believe something wrong.", "Gây hiểu lầm", "flashcard_misleading"),
        Flashcard("Misrepresentation", "Lying to get something.", "Xuyên tạc / Khai gian", "flashcard_misrepresentation"),
        Flashcard("Narcotics", "Illegal drugs.", "Ma túy", "flashcard_narcotics"),
        Flashcard("Nobility", "High social rank (like Duke, Prince).", "Giới quý tộc", "flashcard_nobility"),
        Flashcard("Noncitizen", "Not a citizen.", "Người không phải công dân", "flashcard_noncitizen"),
        Flashcard("Noncombatant", "In army but no fighting.", "Phục vụ không chiến đấu", "flashcard_noncombatant"),
        Flashcard("Nonresident", "Not living in the U.S.", "Người không thường trú", "flashcard_nonresident"),
        Flashcard("Oath", "A promise.", "Lời thề", "flashcard_oath"),
        Flashcard("Oath of Allegiance", "A promise to be loyal to the U.S.", "Lời tuyên thệ trung thành", "flashcard_oath_of_allegiance"),
        Flashcard("Obtain", "To get something.", "Đạt được / Có được", "flashcard_obtain"),
        Flashcard("Occur", "To happen.", "Xảy ra", "flashcard_occur"),
        Flashcard("Officer", "Person with authority.", "Sĩ quan / Cán bộ", "flashcard_officer"),
        Flashcard("Official", "A person with authority.", "Viên chức / Quan chức", "flashcard_official"),
        Flashcard("Opposition", "Being against something.", "Sự chống đối / Đối lập", "flashcard_opposition"),
        Flashcard("Order of Nobility", "A high rank given by a King.", "Tước vị quý tộc", "flashcard_order_of_nobility"),
        Flashcard("Organization", "An organized group of people.", "Tổ chức", "flashcard_organization"),
        Flashcard("Overdue", "Late (not paid on time).", "Quá hạn", "flashcard_overdue"),
        Flashcard("Overthrow", "To remove government by force.", "Lật đổ chính quyền", "flashcard_overthrow"),
        Flashcard("Owe", "To need to pay money.", "Nợ", "flashcard_owe"),
        Flashcard("Paramilitary Unit", "Civilians acting like an army.", "Nhóm bán quân sự", "flashcard_paramilitary_unit"),
        Flashcard("Paraphernalia", "Tools for using drugs.", "Dụng cụ hút chích", "flashcard_paraphernalia"),
        Flashcard("Pardon", "Official forgiveness for a crime.", "Lệnh ân xá (tha tội hoàn toàn)", "flashcard_pardon"),
        Flashcard("Parole", "Early release from prison.", "Tha tù trước thời hạn", "flashcard_parole"),
        Flashcard("Participate", "To be part of or join in.", "Tham gia", "flashcard_participate"),
        Flashcard("Pending", "Waiting for a decision.", "Đang chờ xử lý", "flashcard_pending"),
        Flashcard("Permanent", "Lasting forever.", "Vĩnh viễn / Thường trú", "flashcard_permanent"),
        Flashcard("Persecute", "To hurt because of race/religion.", "Đàn áp / Bức hại", "flashcard_persecute"),
        Flashcard("Police Unit", "Official law enforcement group.", "Đơn vị cảnh sát", "flashcard_police_unit"),
        Flashcard("Political Opinion", "What you think about politics.", "Quan điểm chính trị", "flashcard_political_opinion"),
        Flashcard("Polygamy", "Married to more than one person.", "Chế độ đa thê", "flashcard_polygamy"),
        Flashcard("Practice", "To do or follow (a religion).", "Thực hành / Theo (tôn giáo)", "flashcard_practice"),
        Flashcard("Prepare", "To get ready.", "Chuẩn bị", "flashcard_prepare"),
        Flashcard("Prison Camp", "Camp for prisoners of war/politics.", "Trại tù binh / Trại tập trung", "flashcard_prison_camp"),
        Flashcard("Prisoner", "Person kept in prison.", "Tù nhân", "flashcard_prisoner"),
        Flashcard("Probation", "No jail, but must report to police.", "Quản chế (tù treo)", "flashcard_probation"),
        Flashcard("Proceedings", "A legal case or process.", "Thủ tục tố tụng / Vụ kiện", "flashcard_proceedings"),
        Flashcard("Proceeds", "Money earned (usually illegal).", "Tiền thu được", "flashcard_proceeds"),
        Flashcard("Procure", "To find someone (for prostitution).", "Môi giới / Dắt mối", "flashcard_procure"),
        Flashcard("Property", "Things you own.", "Tài sản", "flashcard_property"),
        Flashcard("Prostitution", "Sex for money.", "Mại dâm", "flashcard_prostitution"),
        Flashcard("Provide", "To give.", "Cung cấp", "flashcard_provide"),
        Flashcard("Public Benefit", "Money or help from government.", "Trợ cấp xã hội", "flashcard_public_benefit"),
        Flashcard("Purpose", "Reason.", "Mục đích", "flashcard_purpose"),
        Flashcard("Question", "Something you ask.", "Câu hỏi", "flashcard_question"),
        Flashcard("Race", "Ethnic group or background.", "Chủng tộc", "flashcard_race"),
        Flashcard("Reason", "Why something happens.", "Lý do", "flashcard_reason"),
        Flashcard("Rebel Group", "Fighting against the government.", "Nhóm phiến quân", "flashcard_rebel_group"),
        Flashcard("Receive", "To get.", "Nhận", "flashcard_receive"),
        Flashcard("Records", "Written accounts or files.", "Hồ sơ", "flashcard_records"),
        Flashcard("Recruit", "To ask someone to join.", "Tuyển mộ", "flashcard_recruit"),
        Flashcard("Register", "To sign up.", "Đăng ký", "flashcard_register"),
        Flashcard("Regulation", "Official rule or law.", "Quy định", "flashcard_regulation"),
        Flashcard("Rehabilitative Program", "Program to help cure drugs/alcohol.", "Chương trình phục hồi nhân phẩm", "flashcard_rehabilitative_program"),
        Flashcard("Religion", "Belief in God.", "Tôn giáo", "flashcard_religion"),
        Flashcard("Removal", "Being taken out of the U.S.", "Bị trục xuất / Di lý", "flashcard_removal"),
        Flashcard("Rescission", "Taking back a decision.", "Thu hồi / Hủy bỏ", "flashcard_rescission"),
        Flashcard("Resident", "Person living in a place.", "Cư dân / Người thường trú", "flashcard_resident"),
        Flashcard("Residing", "Living in.", "Đang sinh sống / Cư trú", "flashcard_residing"),
        Flashcard("Return", "To go back or a tax form.", "Trở lại / Tờ khai thuế", "flashcard_return"),
        Flashcard("Sabotage", "Destroying things on purpose.", "Phá hoại", "flashcard_sabotage"),
        Flashcard("Sealed", "Hidden or closed record.", "Hồ sơ bị niêm phong", "flashcard_sealed"),
        Flashcard("Selective Service", "Agency for military draft registration.", "Hệ thống Nghĩa vụ Quân sự", "flashcard_selective_service"),
        Flashcard("Self-defense Unit", "A group protecting themselves.", "Đơn vị tự vệ", "flashcard_self_defense_unit"),
        Flashcard("Separation", "Living apart but married.", "Ly thân", "flashcard_separation"),
        Flashcard("Served", "Worked in.", "Phục vụ", "flashcard_served"),
        Flashcard("Severely", "Very badly.", "Nghiêm trọng / Dữ dội", "flashcard_severely"),
        Flashcard("Sexual Contact", "Touching private parts.", "Quan hệ tình dục", "flashcard_sexual_contact"),
        Flashcard("Smuggle", "To hide and bring illegal things.", "Buôn lậu", "flashcard_smuggle"),
        Flashcard("Social Group", "People sharing similar characteristics.", "Nhóm xã hội", "flashcard_social_group"),
        Flashcard("Specify", "To state clearly.", "Ghi rõ / Chỉ rõ", "flashcard_specify"),
        Flashcard("Stationed", "Where a soldier works.", "Đóng quân", "flashcard_stationed"),
        Flashcard("Suffering", "Feeling pain or distress.", "Đau khổ", "flashcard_suffering"),
        Flashcard("Supplies", "Things needed (food/tools).", "Nhu yếu phẩm / Tiếp tế", "flashcard_supplies"),
        Flashcard("Support", "To pay for help.", "Chu cấp / Nuôi dưỡng", "flashcard_support"),
        Flashcard("Suspended Sentence", "Delayed jail sentence.", "Án treo", "flashcard_suspended_sentence"),
        Flashcard("Taxes", "Money paid to government.", "Thuế", "flashcard_taxes"),
        Flashcard("Threaten", "To say you will hurt someone.", "Đe dọa", "flashcard_threaten"),
        Flashcard("Torture", "Causing great pain.", "Tra tấn", "flashcard_torture"),
        Flashcard("Totalitarian Party", "Dictatorship. No freedom.", "Đảng độc tài", "flashcard_totalitarian_party"),
        Flashcard("Trafficked", "Buying or selling people/drugs.", "Buôn bán trái phép (người/ma túy)", "flashcard_trafficked"),
        Flashcard("Training", "Learning how to do something.", "Huấn luyện / Đào tạo", "flashcard_training"),
        Flashcard("Transport", "To move things.", "Vận chuyển", "flashcard_transport"),
        Flashcard("Transportation", "Moving things.", "Vận chuyển / Giao thông", "flashcard_transportation"),
        Flashcard("Unconstitutional", "Not allowed by the Constitution.", "Vi hiến / Trái Hiến pháp", "flashcard_unconstitutional"),
        Flashcard("Unlawful", "Against the law; illegal.", "Bất hợp pháp / Trái pháp luật", "flashcard_unlawful"),
        Flashcard("Use", "To do something with tool.", "Sử dụng", "flashcard_use"),
        Flashcard("Vehicle", "Car, truck, or bus.", "Phương tiện / Xe cộ", "flashcard_vehicle"),
        Flashcard("Vessel", "A ship or boat.", "Tàu thuyền lớn", "flashcard_vessel"),
        Flashcard("Vigilante Unit", "Acting like police illegally.", "Nhóm dân phòng tự phát", "flashcard_vigilante_unit"),
        Flashcard("Violation", "Breaking a rule.", "Sự vi phạm", "flashcard_violation"),
        Flashcard("Violence", "Using force to hurt people.", "Bạo lực", "flashcard_violence"),
        Flashcard("Volunteer", "To work without pay.", "Tình nguyện", "flashcard_volunteer"),
        Flashcard("Vote", "To choose a leader.", "Bầu cử", "flashcard_vote"),
        Flashcard("War", "Fighting between countries.", "Chiến tranh", "flashcard_war"),
        Flashcard("Way", "Method.", "Cách thức / Con đường", "flashcard_way"),
        Flashcard("Weapon", "Gun, knife, or bomb.", "Vũ khí", "flashcard_weapon"),
        Flashcard("Willing", "Ready to do something.", "Sẵn lòng / Tự nguyện", "flashcard_willing"),
        Flashcard("Withheld Adjudication", "Judge delays the final decision.", "Hoãn phán quyết", "flashcard_withheld_adjudication"),
        Flashcard("Work of National Importance", "Helping country in emergency.", "Công việc quan trọng quốc gia", "flashcard_work_of_national_importance")
    ).sortedBy { it.word }

    private val _uiState = MutableStateFlow(
        FlashcardUiState(
            allFlashcards = originalList,
            flashcards = originalList,
            selectedWords = originalList.map { it.word }.toSet()
        )
    )
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    fun nextCard() {
        _uiState.update { state ->
            if (state.flashcards.isEmpty()) {
                return@update state
            }
            val nextIndex = (state.currentIndex + 1) % state.flashcards.size
            state.copy(currentIndex = nextIndex)
        }
    }

    fun prevCard() {
        _uiState.update { state ->
            if (state.flashcards.isEmpty()) {
                return@update state
            }
            val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.flashcards.size - 1
            state.copy(currentIndex = prevIndex)
        }
    }

    fun toggleShuffle() {
        _uiState.update { state ->
            val newIsShuffled = !state.isShuffled
            state.copy(
                flashcards = buildDeck(state.selectedWords, newIsShuffled),
                isShuffled = newIsShuffled,
                currentIndex = 0 // Reset to first card when shuffling/unshuffling
            )
        }
    }

    fun toggleWordSelection(word: String) {
        _uiState.update { state ->
            val selectedWords = if (word in state.selectedWords) {
                state.selectedWords - word
            } else {
                state.selectedWords + word
            }
            state.copy(
                flashcards = buildDeck(selectedWords, state.isShuffled),
                selectedWords = selectedWords,
                currentIndex = 0
            )
        }
    }

    fun selectAllWords() {
        _uiState.update { state ->
            val selectedWords = originalList.map { it.word }.toSet()
            state.copy(
                flashcards = buildDeck(selectedWords, state.isShuffled),
                selectedWords = selectedWords,
                currentIndex = 0
            )
        }
    }

    fun clearSelectedWords() {
        _uiState.update { state ->
            state.copy(
                flashcards = emptyList(),
                selectedWords = emptySet(),
                currentIndex = 0
            )
        }
    }

    fun toggleSpeed() {
        _uiState.update { state ->
            val nextSpeed = when (state.ttsSpeed) {
                0.5f -> 0.85f
                0.85f -> 1.1f
                else -> 0.5f
            }
            state.copy(ttsSpeed = nextSpeed)
        }
    }

    fun toggleVoicePreference() {
        _uiState.update { state ->
            state.copy(preferOnlineVoice = !state.preferOnlineVoice)
        }
    }

    private fun fisherYatesShuffle(list: List<Flashcard>): List<Flashcard> {
        val result = list.toMutableList()
        for (i in result.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            val temp = result[i]
            result[i] = result[j]
            result[j] = temp
        }
        return result
    }

    private fun buildDeck(selectedWords: Set<String>, isShuffled: Boolean): List<Flashcard> {
        val selectedCards = originalList.filter { it.word in selectedWords }
        return if (isShuffled) fisherYatesShuffle(selectedCards) else selectedCards
    }
}
