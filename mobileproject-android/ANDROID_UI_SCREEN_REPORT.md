# Báo cáo phân tích Android UI - Affinity

## 1. Phạm vi và cách đọc

Tài liệu này chỉ tập trung vào phần Android trong `mobileproject-android`, dựa trên việc đọc source code hiện tại của project.

Mục tiêu của báo cáo:
- Giải thích cấu trúc từng màn hình Android.
- Chỉ ra màn đó được ghép từ những component nào.
- Phân biệt đâu là màn hình chính, đâu là reusable component, đâu là flow phụ.
- Phân tích vì sao team lại chọn cấu trúc UI như vậy.

Giới hạn:
- Đây là phân tích tĩnh từ source code, chưa phải kết quả chạy thực tế trên thiết bị.
- Một số text trong code đang lỗi encoding, nhưng cấu trúc UI và ý đồ thiết kế vẫn đọc được khá rõ.

---

## 2. Bản đồ điều hướng tổng quát

### 2.1 Entry activity

- `presentation/ui/screen/welcome/WelcomeActivity.kt`
  - Launcher entry point của app.
  - Kiểm tra session trước.
  - Nếu đã đăng nhập nhưng profile chưa đầy đủ thì chuyển sang `PersonalInfoActivity`.
  - Nếu đã đủ profile thì vào `HomeActivity`.
  - Nếu chưa có session thì hiển thị `WelcomeScreen`.

### 2.2 App shell chính sau đăng nhập

- `presentation/ui/screen/home/HomeActivity.kt`
  - Bootstrap theme, session, FCM, immersive mode.
  - Render `HomeScaffold(...)`.

- `presentation/ui/screen/app/compose/HomeScaffold.kt`
  - Đây là shell thật của app Android.
  - Nắm `NavHost`, `HomeTopBar`, `AppNavigationBar`, snackbar, in-app notification, xử lý back press.
  - Điều hướng các route:
    - `home`
    - `explore`
    - `wallet`
    - `memories`
    - `profile`
    - `chat`
    - `add_expense`
    - `top_up`
    - `top_up_qr/{topUpId}`
    - `top_up_bank_redirect/{topUpId}`
    - `transfer_money`
    - `recent_transactions`
    - `saving_goals`
    - `future_goals`
    - `add_saving_goal`
    - `add_future_goal`
    - `notifications`
    - `settings`
    - `settings_privacy`
    - `settings_notifications`
    - `settings_appearance`
    - `settings_help`
    - `profile_edit`
    - `memories_capture`

### 2.3 Thanh điều hướng chính

- `presentation/ui/navigation/NavigationConfig.kt`
  - 5 tab gốc: `home`, `explore`, `wallet`, `memories`, `profile`.

- `presentation/ui/navigation/AppNavigationBar.kt`
  - Bottom bar custom.
  - Mỗi tab gồm icon + label.
  - Có indicator line mảnh phía trên tab đang active.
  - Dùng `animateColorAsState` để icon/label đổi màu khi chọn.

### 2.4 Top bar dùng chung

- `presentation/ui/screen/app/compose/HomeTopBar.kt`
  - Title thay đổi theo route.
  - Trái: profile icon hoặc close icon tùy route.
  - Phải: bell notification có badge và animation rung khi có unread.

### 2.5 Quy ước shell

Team cố ý ẩn top bar và bottom bar ở các flow cần tập trung:
- `chat`
- `add_expense`
- `top_up`
- `top_up_qr`
- `top_up_bank_redirect`
- `recent_transactions`
- `saving_goals`
- `future_goals`
- `add_saving_goal`
- `add_future_goal`
- `transfer_money`
- `profile_edit`
- `notifications`
- các settings detail
- `memories_capture`

Ý nghĩa:
- Màn overview giữ navigation chrome để điều hướng ngang.
- Màn thao tác cụ thể hoặc nhạy cảm được tách thành focused flow, tránh loãng trải nghiệm.

---

## 3. Design system và reusable foundation

### 3.1 Visual direction chung

App đi theo hướng:
- mềm
- ấm
- “relationship first”
- không giống fintech lạnh lẽo

Những dấu hiệu rõ:
- palette hồng/đỏ/kem
- bo góc lớn
- nhiều `Surface`/`Card` dạng rounded
- gradient nền, radial glow
- mascot/sticker/heart/ribbon
- dark mode vẫn giữ undertone ấm

### 3.2 Reusable nền và card

- `presentation/ui/components/core/AppUiKit.kt`
  - `AppScreenBackground`: nền gradient + 2 radial glow.
  - `AppSurfaceCard`: card chung dùng cho explore/memories/settings.
  - `AppSectionHeader`: title + subtitle.
  - `AppPrimaryButton`: button chính đồng bộ style.
  - `AppFormTextField`: input field chuẩn cho flow hiện đại.
  - `AppStateMessage`: state card cho error/empty/info.
  - `AppMetricChip`: chip metric nhỏ.
  - `AppEmptyState`: empty card.

- `presentation/ui/components/auth/AuthVisuals.kt`
  - `AuthBackdrop`: nền gradient riêng cho auth/onboarding.
  - `AuthBrandMark`: logo trái tim dạng pulse.
  - `AuthFormSurface`: form card trắng bo lớn.

### 3.3 Reusable theo feature

- `presentation/ui/screen/home/components/GoalCard.kt`
  - Card dùng cho cả saving goal và future goal.

- `presentation/ui/screen/home/components/ContributeGoalBottomSheet.kt`
  - Bottom sheet góp tiền vào goal, dùng lại từ `Home` và `Wallet`.

- `presentation/ui/components/place/PlaceCards.kt`
  - `PlaceCard`
  - `TrendingPlaceCard`
  - quy chuẩn card ảnh lớn cho Explore.

- `presentation/ui/component/wallet/*`
  - `BalanceSection`
  - `MonthlySpendingCard`
  - `RecentActivitySection`
  - `TransactionItem`

### 3.4 Vì sao shared component quan trọng

Lý do team tách component như vậy:
- Giữ ngôn ngữ thị giác nhất quán giữa nhiều màn.
- Dễ ship feature mới mà không phải dựng lại UI từ đầu.
- Giảm sự lệch phong cách giữa các nhóm màn.
- Cho phép một component domain như `GoalCard` xuất hiện ở cả `Home`, `Wallet`, `Goal list`.

---

## 4. Phân tích chi tiết từng màn hình

## 4.1 `WelcomeScreen`

- File:
  - `presentation/ui/screen/welcome/WelcomeScreen.kt`
- Entry:
  - được gọi từ `WelcomeActivity`
- State:
  - gần như stateless, chỉ có `rememberScrollState`
- Cấu trúc ghép UI:
  1. `BoxWithConstraints` để biết màn nhỏ hay lớn.
  2. `Box` full screen với nền `primaryContainer`.
  3. Một `Box` viền trắng rất dày bo 40dp đóng vai trò “frame”.
  4. `Column` chính chia làm 2 tầng:
     - tầng hero minh họa
     - tầng CTA card
  5. Hero layer gồm:
     - tim nhỏ trang trí
     - mascot trung tâm
     - ribbon + ribbon text
  6. CTA card gồm:
     - title 2 dòng
     - subtitle
     - primary button `Get Started`
     - link `Login`
- Component chính:
  - `BoxWithConstraints`
  - `Image`
  - `Button`
  - `Text`
- Vì sao thiết kế như vậy:
  - Đây là màn branding, không phải màn thao tác.
  - Team ưu tiên cảm xúc ngay từ entry: mascot, ribbon, frame mềm.
  - CTA chỉ có 2 hướng rõ ràng để giảm decision load.
  - `BoxWithConstraints` + optional scroll giúp an toàn trên máy thấp.

## 4.2 `LoginScreen`

- File:
  - `presentation/ui/screen/login/LoginActivity.kt`
- Entry:
  - `LoginActivity`
- ViewModel:
  - `AuthViewModel`
- State cục bộ:
  - `email`
  - `password`
  - `passwordVisible`
  - `showRegistrationSuccess`
- Cấu trúc ghép UI:
  1. `BoxWithConstraints` nền trống.
  2. 2 ảnh rabbit/fox đặt dưới trái và dưới phải như background character.
  3. `Column` toàn màn, có scroll fallback.
  4. Một floating card lớn:
     - title
     - subtitle
     - success message nếu vừa register xong
     - email field
     - password field
     - forgot password link
     - primary login button
     - row chuyển sang sign up
- Component con:
  - `LoginInputField`
  - `Surface` cho success message
  - `IconButton` toggle password
- Vì sao thiết kế như vậy:
  - Login là form utilitarian nhưng app vẫn muốn giữ tone “cute/soft”.
  - Card nổi trên mascot giúp form dễ đọc mà vẫn không mất chất thương hiệu.
  - Thành công sau register được giữ lại ở màn login để đóng vòng onboarding mượt.

## 4.3 `RegisterScreen`

- File:
  - `presentation/ui/screen/register/RegisterActivity.kt`
- ViewModel:
  - `AuthViewModel`
- State:
  - `email`, `nickname`, `password`, `birthday`, `phoneNumber`
  - `passwordVisible`
  - `validationError`
- Cấu trúc:
  1. Dùng cùng bố cục nền rabbit/fox như login.
  2. `Column` chứa 1 floating form card.
  3. Trong card:
     - title
     - subtitle
     - email
     - nickname
     - password
     - birthday
     - phone number
     - error
     - CTA đăng ký
     - link quay lại login
- Component con:
  - `RegisterInputField`
- Vì sao:
  - Team giữ continuity mạnh với login để người dùng không cảm giác bị chuyển sang app khác.
  - Form dài nên bật scroll sớm hơn login.
  - Về mặt UI, card trắng mờ giữ tính dễ đọc cho form nhiều trường.

## 4.4 `PersonalInfoScreen`

- File:
  - `presentation/ui/screen/profile/PersonalInfoActivity.kt`
  - `presentation/ui/screen/profile/ProfileFormContent.kt`
- ViewModel:
  - `ProfileViewModel`
- Cấu trúc:
  1. `AuthBackdrop`
  2. `Column` trung tâm
  3. `AuthBrandMark`
  4. title + subtitle profile setup
  5. `AuthFormSurface`
  6. Bên trong dùng `ProfileFormContent`:
     - full name
     - nickname
     - birth date
     - gender chips
     - save button
  7. Error message hiển thị ngay trong form surface
- Vì sao:
  - Đây là cầu nối giữa auth và app shell.
  - Team dùng lại visual auth để người dùng thấy đây vẫn là onboarding.
  - Nhưng nội dung đã đổi sang profile normalization vì các feature sau phụ thuộc dữ liệu này.

## 4.5 `HomeScaffold` và app shell

- File:
  - `presentation/ui/screen/app/compose/HomeScaffold.kt`
  - `presentation/ui/screen/app/compose/HomeTopBar.kt`
  - `presentation/ui/navigation/AppNavigationBar.kt`
- Vai trò:
  - Không phải màn nội dung, mà là khung vận hành toàn app.
- Cấu trúc:
  1. Xác định route hiện tại.
  2. Tính route nào ẩn top/bottom bar.
  3. Tạo nền gradient + radial glow.
  4. `Scaffold`
     - `topBar = HomeTopBar`
     - `bottomBar = AppNavigationBar`
     - `snackbarHost`
  5. `NavHost` gắn tất cả composable route.
  6. Gắn event bus cho in-app notification.
  7. Gắn behavior back press theo ngữ cảnh.
- Vì sao:
  - Tách shell riêng giúp các màn business không phải tự xử lý navigation chrome.
  - Route-level chrome hiding là lựa chọn đúng cho app nhiều feature nhưng muốn behavior nhất quán.

## 4.6 `HomeScreen`

- File:
  - `presentation/ui/screen/home/HomeScreen.kt`
- ViewModel:
  - `CoupleViewModel`
  - `WalletViewModel`
  - `GoalViewModel`
  - `HomeMapShareViewModel`
- State cục bộ đáng chú ý:
  - `isFabExpanded`
  - `isContributeSheetVisible`
  - state map marker / last location / center behavior
- Cấu trúc:
  1. Nạp dữ liệu couple, wallet, goal, map share.
  2. Xử lý permission location và lifecycle cho foreground map service.
  3. `Box` ngoài cùng.
  4. `HomeContent` bên trong là `Column` scroll dọc.
  5. Thứ tự khối chính:
     - `SharedBalanceCard`
     - `DaysTogetherModernCard`
     - row `MiniMetricCard`
     - `GoalListSection` cho future goals
     - `GoalListSection` cho saving goals
     - `PairSection` nếu chưa ghép đôi
     - hoặc card map share nếu đã ghép đôi
  6. Khối map nếu đã paired:
     - card bật/tắt share location
     - map card `AndroidView(MapView)`
     - badge khoảng cách
     - 2 nút mini FAB center me / center partner
  7. Phần hành động nổi:
     - speed dial từ FAB
     - gọi `ContributeGoalBottomSheet` khi cần
- Component con quan trọng:
  - `SharedBalanceCard`
  - `DaysTogetherModernCard`
  - `MiniMetricCard`
  - `GoalListSection`
  - `GoalCard`
  - `PairSection`
  - `ContributeGoalBottomSheet`
- Vì sao:
  - Đây là dashboard “mối quan hệ dùng chung”, không chỉ dashboard ví.
  - Team đặt balance + days together cạnh nhau để gắn tài chính với relationship context.
  - Goals được đặt trước map vì mục tiêu là trục hành vi, map là trục trạng thái realtime.
  - Pairing và map cùng một slot là quyết định tốt: cùng nói về “connected state”.

## 4.7 `CoupleConnectContent`

- File:
  - `presentation/ui/screen/couple/CoupleConnectScreen.kt`
- Entry:
  - activity thật dùng `CoupleConnectActivity`, nhưng nội dung chính render `CoupleConnectContent(...)`
- ViewModel:
  - `CoupleViewModel`
- Cấu trúc:
  1. Ảnh background full screen.
  2. Overlay gradient tối.
  3. `LazyColumn` trung tâm.
  4. Hero section:
     - heart pulse
     - title
     - subtitle
  5. Card glass option 1:
     - generated couple code
     - nút copy/share
     - trạng thái request outgoing
  6. Divider
  7. Card glass option 2:
     - input partner code
     - CTA gửi lời mời
  8. Nếu có incoming request:
     - card accept / reject
- Vì sao:
  - Pairing là flow giàu cảm xúc, nên visual cinematic phù hợp hơn màn form thuần.
  - Hai luồng “tạo mã” và “nhập mã” được đặt song song để khớp 2 mental model người dùng.
  - Overlay tối giúp chữ luôn readable dù background ảnh lớn.

## 4.8 `CoupleConnectedScreen`

- File:
  - `presentation/ui/screen/couple/CoupleConnectedActivity.kt`
- Cấu trúc:
  1. Nền gradient đơn giản.
  2. Emoji celebration.
  3. title + subtitle.
  4. card minh họa lớn với heart icon.
  5. chip ngày bắt đầu nếu có.
  6. button về home.
- Vì sao:
  - Đây là success confirmation screen.
  - Thiết kế intentionally đơn giản để nhấn vào cảm giác “đã kết nối”, không làm người dùng phân tán.

## 4.9 `ExploreScreen`

- File:
  - `presentation/ui/screen/explore/ExploreScreen.kt`
  - `presentation/ui/components/place/PlaceCards.kt`
- ViewModel:
  - `ExploreViewModel`
  - `FavoriteViewModel`
  - `HistoryViewModel`
- Cấu trúc:
  1. `ExploreRoute` inject viewmodel rồi chuyển vào `ExploreScreen`.
  2. Lấy permission location nếu cần `near me`.
  3. `AppScreenBackground`.
  4. `LazyColumn` làm khung tổng.
  5. Các section theo thứ tự:
     - `AppSectionHeader`
     - block tìm kiếm và lọc
       - `ExploreSearchBar`
       - `ExploreTypeChipsRow`
       - `ExploreAdvancedFilters`
     - `ExploreBudgetPlannerCard`
     - budget result cards nếu plan có dữ liệu
     - random suggestion card
     - trending carousel bằng `TrendingPlaceCard`
     - results counter
     - linear loading state
     - place feed bằng `PlaceCard`
  6. Detail place hiển thị qua `PlaceDetailBottomSheet`.
  7. Budget result card có action:
     - xem detail
     - gửi vào chat
     - mở Google Maps
- Component con nổi bật:
  - `AppSurfaceCard`
  - `ExploreBudgetPlannerCard`
  - `ExploreBudgetPlanItemCard`
  - `PlaceCard`
  - `TrendingPlaceCard`
  - `PlaceDetailBottomSheet`
- Vì sao:
  - Explore được thiết kế như một “workflow”, không phải một list.
  - Search, discover, budget, share được đặt trên cùng một màn để hỗ trợ planning cho cặp đôi.
  - Bottom sheet detail giữ context của feed, tránh chuyển màn liên tục.
  - `PlaceCard` ảnh lớn và overlay mạnh vì quyết định địa điểm là quyết định cảm tính, hình ảnh phải dẫn trước text.

## 4.10 `WalletScreen`

- File:
  - `presentation/ui/screen/wallet/WalletScreen.kt`
  - `presentation/ui/component/wallet/*`
- ViewModel:
  - `WalletViewModel`
  - `SavingGoalViewModel`
- Cấu trúc:
  1. Load wallet + goals.
  2. `Box` chứa toàn bộ màn.
  3. `Scaffold` với content là `LazyColumn`.
  4. Thứ tự card:
     - `BalanceSection`
     - `MonthlySpendingCard`
     - `RecentActivitySection`
  5. Overlay month picker dialog card khi chọn tháng.
  6. Speed-dial FAB mở ra 4 action:
     - contribute to goal
     - add expense
     - top up
     - transfer
  7. Nếu contribute thì mở `ContributeGoalBottomSheet`.
- Vì sao:
  - Wallet là dashboard hành động.
  - Team đặt số dư lên đầu, chart ở giữa, lịch sử gần đây bên dưới, đúng thứ tự “nắm tình hình -> hiểu xu hướng -> xem giao dịch”.
  - Các hành động không nằm thành nhiều button cố định trên màn, mà gom vào FAB để giao diện overview vẫn sạch.

## 4.11 `AddExpenseScreen`

- File:
  - `presentation/ui/screen/add_expense/AddExpenseScreen.kt`
  - `presentation/ui/screen/add_expense/components/*`
- ViewModel:
  - `AddExpenseViewModel`
- Cấu trúc:
  1. `Scaffold` với top bar hiển thị available balance.
  2. `Column` scroll.
  3. Các khối theo thứ tự:
     - `AmountInput`
     - `CategorySelector`
     - `ExpenseFormFields`
     - `AttachReceiptSection`
  4. Footer button cố định phía dưới.
  5. `CategoryBottomSheet` mở khi bấm more category.
- Vì sao:
  - Đây là một form nhập chi tiêu trực tiếp.
  - Thứ tự field rất hợp lý: số tiền -> loại -> note/date -> chứng từ.
  - Footer button cố định giúp CTA luôn trong tầm tay.
  - Card attach receipt hiện là placeholder, chứng tỏ team đã chừa không gian cho feature mở rộng.

## 4.12 `TopUpScreen`

- File:
  - `presentation/ui/screen/wallet/TopUpScreen.kt`
- ViewModel:
  - `TopUpViewModel`
- Cấu trúc:
  1. `Scaffold` với top bar có back behavior theo step.
  2. `AnimatedContent` chuyển giữa 2 step:
     - `AmountStep`
     - `BankSelectStep`
  3. `AmountStep` gồm:
     - amount input lớn ở trung tâm
     - note field
     - predicted balance card
     - CTA sang chọn bank
  4. `BankSelectStep` gồm:
     - summary amount
     - list bank
     - CTA tạo top-up request
- Vì sao:
  - Nạp tiền là flow nhạy cảm, tách 2 step giúp người dùng ít nhầm hơn.
  - Amount được làm cực lớn để nhấn hành động chính.
  - Animated transition giúp step change mượt mà như wizard.

## 4.13 `TopUpQRScreen`

- File:
  - `presentation/ui/screen/wallet/TopUpQRScreen.kt`
- ViewModel:
  - `TopUpViewModel`
- Cấu trúc:
  1. Poll trạng thái top-up theo `topUpId`.
  2. `Scaffold` với top bar.
  3. `Column` scroll.
  4. Nếu chưa có data:
     - `LoadingTopUpState`
  5. Nếu có data:
     - bank row
     - `TopUpStatusBadge`
     - QR card
     - countdown hết hạn
     - `TransferDetailsCard`
     - error text nếu có
     - button refresh status
     - helper note về backend chỉ cộng tiền khi webhook xác nhận
- Vì sao:
  - UI bám sát cơ chế thật của bank transfer và webhook.
  - QR phải là hero element vì đó là vật thể tương tác chính.
  - Countdown làm rõ ràng tính tạm thời của mã QR.

## 4.14 `TopUpBankRedirectScreen`

- File:
  - `presentation/ui/screen/wallet/TopUpBankRedirectScreen.kt`
- ViewModel:
  - `TopUpViewModel`
- Cấu trúc:
  1. Có `phase` để biểu diễn:
     - preparing
     - waiting
     - success
  2. `Scaffold` tối giản.
  3. Màn giữa thay đổi theo trạng thái:
     - spinner khi chuẩn bị
     - waiting card + `TransferSummary`
     - success state với checkmark scale animation
  4. CTA quay lại ví khi thành công.
- Vì sao:
  - Khi user rời sang app ngân hàng, mental model không còn là “quét QR tại đây”.
  - Team tách màn riêng để nói rõ app đang chờ xác nhận ngoài app.
  - Việc có phase giúp màn chờ không bị cảm giác treo.

## 4.15 `TransferMoneyScreen`

- File:
  - `presentation/ui/screen/wallet/TransferMoneyScreen.kt`
- ViewModel:
  - `TransferMoneyViewModel`
- Cấu trúc:
  1. Nhận optional `scannedQrRaw`.
  2. Resolve QR sang account/bank/amount/note nếu có.
  3. `Scaffold` với snackbar.
  4. Nếu success:
     - render `SuccessContent`
  5. Nếu chưa success:
     - `WaitingBankCard` nếu đang chờ confirm
     - info card mô tả transfer thủ công
     - account number field
     - bank field read-only + `BankSelectionSheet`
     - amount field
     - note field
     - text button tạo note mặc định
     - CTA confirm
- Vì sao:
  - Đây là flow payout/transfer dạng form, nên UI phải nghiêng về độ chính xác hơn cảm xúc.
  - Tách `BankSelectionSheet` là hợp lý vì danh sách ngân hàng dài.
  - Hỗ trợ autofill từ QR giúp giảm lỗi nhập tay.

## 4.16 `QRScannerScreen`

- File:
  - `presentation/ui/screen/wallet/QRScannerScreen.kt`
- Cấu trúc:
  1. Xin permission camera.
  2. `Scaffold` với top bar.
  3. Vùng preview chính:
     - state chưa có quyền
     - hoặc `CameraQrPreview`
  4. Vùng báo lỗi nếu scan fail.
  5. Footer:
     - nút chọn ảnh từ gallery
     - nút nhập thủ công
- Vì sao:
  - Scanner là tool screen, không cần nhiều trang trí.
  - Team vẫn chừa fallback gallery + manual input, rất đúng cho thực tế QR ở Việt Nam.

## 4.17 `RecentTransactionsScreen`

- File:
  - `presentation/ui/screen/wallet/RecentTransactionsScreen.kt`
- ViewModel:
  - `WalletViewModel`
- Cấu trúc:
  1. `Scaffold` top bar.
  2. `Box` body.
  3. 3 trạng thái:
     - loading
     - empty
     - `LazyColumn` transaction list
  4. Mỗi item dùng `TransactionItem`.
- Vì sao:
  - Đây là drill-down screen từ `WalletScreen`.
  - Cố tình rất đơn giản để giữ người dùng tập trung vào dữ liệu.

## 4.18 `SavingGoalsScreen`

- File:
  - `presentation/ui/screen/wallet/SavingGoalsScreen.kt`
- ViewModel:
  - `SavingGoalViewModel`
- Cấu trúc:
  1. `Scaffold` top bar.
  2. loading state nếu cần.
  3. sort goals.
  4. `LazyColumn` render `GoalCard`.
- Vì sao:
  - Màn này là “xem tất cả” của section ở home/wallet.
  - Team tái sử dụng `GoalCard` để continuity giữa overview và detail list.

## 4.19 `FutureGoalsScreen`

- File:
  - `presentation/ui/screen/wallet/FutureGoalsScreen.kt`
- ViewModel:
  - `GoalViewModel`
- Cấu trúc:
  1. `Scaffold` top bar.
  2. lọc `FutureGoal` từ state tổng.
  3. sort.
  4. `LazyColumn` render `GoalCard`.
  5. truyền `onTaskToggle` vào `GoalCard`.
- Vì sao:
  - Future goal khác saving goal ở chỗ có checklist task.
  - Nhưng team vẫn dùng chung card shell, chỉ thay content bên trong.

## 4.20 `AddSavingGoalScreen`

- File:
  - `presentation/ui/screen/home/AddSavingGoalScreen.kt`
- ViewModel:
  - `GoalViewModel`
- Cấu trúc:
  1. Top bar.
  2. Optional `DatePickerDialog`.
  3. Optional `CategoryBottomSheet`.
  4. Form column:
     - goal name
     - target amount
     - quick categories + more
     - target date
     - save CTA
- Vì sao:
  - Đây là form tạo một object domain rất rõ: name, amount, category, deadline.
  - Quick categories giúp thao tác nhanh, còn bottom sheet giữ khả năng mở rộng.
  - Input amount được nhấn mạnh typography lớn để phản ánh bản chất saving goal.

## 4.21 `AddFutureGoalScreen`

- File:
  - `presentation/ui/screen/home/AddFutureGoalScreen.kt`
- ViewModel:
  - `GoalViewModel`
- Cấu trúc:
  1. top bar
  2. date picker
  3. category bottom sheet
  4. form column:
     - goal name
     - danh sách task động
     - quick categories + more
     - target date
     - save CTA
- Vì sao:
  - Team tách riêng khỏi saving goal vì future goal không xoay quanh amount.
  - Dynamic task rows là cách biểu diễn đúng bản chất của goal loại checklist.

## 4.22 `MemoriesScreen`

- File:
  - `presentation/ui/screen/memories/MemoriesScreen.kt`
- ViewModel:
  - `MemoriesViewModel`
- Cấu trúc:
  1. `AppScreenBackground`
  2. `Column` dọc
  3. Ba khối chính:
     - `RecentMomentsPreviewCard`
     - `MemoriesCalendarCard`
     - `PartnerSection`
  4. `SpecialDaysBottomSheet` mở khi bấm lịch special days
- Chi tiết khối:
  - `RecentMomentsPreviewCard`
    - mosaic preview 2-3 ảnh
    - tile `+N` hoặc `Mở`
  - `MemoriesCalendarCard`
    - nhắc ngày đặc biệt gần nhất
    - CTA mở full list special days
  - `PartnerSection`
    - ảnh/sticker + dữ liệu đối phương
- Vì sao:
  - Đây không phải gallery screen truyền thống.
  - Team cố tình làm theo kiểu “editorial snapshot”: ít nhưng đậm cảm xúc.
  - Preview mosaic giúp màn này gọn và giữ được không khí “nhìn lại kỷ niệm”.

## 4.23 `CaptureMomentScreen`

- File:
  - `presentation/ui/screen/memories/CaptureMomentScreen.kt`
- ViewModel:
  - `MemoriesViewModel`
- Cấu trúc:
  1. Xin permission camera.
  2. Bind CameraX preview với `PreviewView`.
  3. `AppScreenBackground`.
  4. `LazyColumn` gồm:
     - top row close/loading
     - camera preview card
     - control row
       - mở grid ảnh
       - nút shutter
       - đổi camera
     - preview lịch sử gần đây
  5. `MomentGridBottomSheet` nếu mở gallery toàn bộ.
- Vì sao:
  - Creation và browsing được đặt cùng một flow.
  - Với feature memories cho cặp đôi, thao tác “chụp nhanh rồi xem lại” phải liền mạch.
  - Camera preview lớn giữ trọng tâm rất đúng cho action screen.

## 4.24 `ProfileScreen`

- File:
  - `presentation/ui/screen/profile/ProfileScreen.kt`
- ViewModel:
  - `ProfileViewModel`
  - `ThemeModeViewModel`
  - `UserSettingsViewModel`
  - `NotificationViewModel`
- Cấu trúc:
  1. `Scaffold` có snackbar.
  2. loading state toàn màn nếu cần.
  3. `Column` scroll.
  4. Các khối theo thứ tự:
     - `ProfileHeroCard`
     - `AnniversaryCard`
     - `ProfileMenuCard`
     - logout button
     - invite partner button nếu chưa paired
     - version text
- `ProfileMenuCard` thực chất gộp các nhóm:
  - appearance
  - privacy
  - notification
  - help
  - mỗi nhóm có dropdown content riêng
- Vì sao:
  - Đây là màn identity + status + preferences tổng hợp.
  - Team không đẩy mọi thứ sang settings riêng, mà giữ các preference gần profile để đúng mental model “my account”.
  - Invite partner ở đây hợp lý vì pairing cũng là trạng thái identity trong app này.

## 4.25 `ProfileEditScreen`

- File:
  - `presentation/ui/screen/profile/ProfileEditScreen.kt`
- ViewModel:
  - `ProfileViewModel`
- Cấu trúc:
  1. `Scaffold` + snackbar.
  2. Header row tự dựng.
  3. `EditableAvatarCard`
     - avatar
     - upload image
     - chọn avatar frame qua `ModalBottomSheet`
  4. `ProfileEditFields`
     - nickname
     - email
     - birth date
  5. account status card
  6. save button
  7. `DatePickerDialog`
- Vì sao:
  - Edit profile là focused task nên cần bỏ bớt nhiễu của profile overview.
  - Tách riêng màn này giúp validation và dirty state rõ ràng hơn.
  - Avatar frame selector là social/personalization layer nên đặt cùng edit profile rất hợp logic.

## 4.26 `SettingsScreen`

- File:
  - `presentation/ui/screen/settings/SettingsScreen.kt`
- Cấu trúc:
  1. `Column` scroll.
  2. `AppSectionHeader`.
  3. `AppSurfaceCard`.
  4. Bên trong là 4 `SettingsMenuItem`:
     - privacy
     - notifications
     - appearance
     - help
- Vì sao:
  - Đây là hub điều hướng chứ không phải màn setting detail.
  - Dùng list item đơn giản giúp người dùng chọn nhóm rất nhanh.

## 4.27 `SettingsPrivacyScreen`

- File:
  - `presentation/ui/screen/settings/SettingsDetailScreens.kt`
- ViewModel:
  - `UserSettingsViewModel`
- Cấu trúc:
  1. `SettingsSectionLayout`
  2. `AppSurfaceCard`
  3. Nhiều `DropdownSettingItem`
     - show activity status
     - searchable by email
     - logout
  4. Bên trong dropdown dùng `SwitchRow` hoặc `Button`
- Vì sao:
  - Nhiều tùy chọn privacy nhỏ được nhóm bằng dropdown để tránh tạo thêm nhiều màn con.

## 4.28 `SettingsNotificationsScreen`

- File:
  - `presentation/ui/screen/settings/SettingsDetailScreens.kt`
- ViewModel:
  - `NotificationViewModel`
  - `UserSettingsViewModel`
- Cấu trúc:
  1. `SettingsSectionLayout`
  2. `AppSurfaceCard`
  3. `DropdownSettingItem` cho từng nhóm:
     - push
     - email
     - chat
     - top up
     - transaction
     - goal
     - memory
  4. Bên trong dùng `SwitchRow`
- Vì sao:
  - App có nhiều loại notification domain-specific.
  - Gộp theo nhóm thay vì một list toggle phẳng giúp thông tin dễ quét hơn.

## 4.29 `SettingsAppearanceScreen`

- File:
  - `presentation/ui/screen/settings/SettingsDetailScreens.kt`
- ViewModel:
  - `ThemeModeViewModel`
- Cấu trúc:
  1. `SettingsSectionLayout`
  2. `AppSurfaceCard`
  3. `DropdownSettingItem`
  4. `SingleChoiceSegmentedButtonRow` cho:
     - system
     - light
     - dark
- Vì sao:
  - Appearance là lựa chọn mutually exclusive, segmented button đúng hơn switch.

## 4.30 `SettingsHelpScreen`

- File:
  - `presentation/ui/screen/settings/SettingsDetailScreens.kt`
- Cấu trúc:
  1. `SettingsSectionLayout`
  2. `AppSurfaceCard`
  3. `DropdownSettingItem` cho:
     - app version
     - terms of service
     - privacy policy
     - open source
  4. Nội dung dropdown là text hoặc button mở link
- Vì sao:
  - Team gom thông tin “pháp lý / metadata / help” về một chỗ để tránh profile screen quá tải.

## 4.31 `NotificationScreen`

- File:
  - `presentation/ui/screen/notification/NotificationScreen.kt`
- ViewModel:
  - `NotificationViewModel`
- Cấu trúc:
  1. `Scaffold` với top bar centered.
  2. Khi mở màn, nếu còn unread thì mark all read.
  3. `LazyColumn` paginated.
  4. Mỗi item dùng `NotificationItem`.
  5. Item hiển thị:
     - icon emoji theo type
     - title
     - body
     - relative time
- Vì sao:
  - App đã có bell badge trên top bar, nên cần inbox screen thật sự chứ không chỉ toast/snackbar.
  - `markAllRead` khi mở màn phù hợp với pattern inbox.

## 4.32 `ChatScreen`

- File:
  - `presentation/ui/screen/chat/ChatScreen.kt`
  - `presentation/ui/screen/chat/ChatMessageGrouping.kt`
- ViewModel:
  - `ChatViewModel`
- Cấu trúc:
  1. Start websocket/session qua `viewModel.start(accessToken)`.
  2. Root `Column`.
  3. `ChatTopBar`
     - avatar đối phương
     - online dot
     - typing subtitle
     - action mở dialog đổi emoji nhanh
  4. Message list:
     - group message bằng `groupMessages(...)`
     - chèn date separator
     - render qua `SwipeableReplyBubble`
  5. Pending message section riêng.
  6. Typing indicator riêng.
  7. Error text riêng.
  8. `ReplyQuoteBar` nếu đang reply.
  9. `MiniAiSuggestionRow` nếu detect mention `@MiniAI`.
  10. `ChatInputBar`
  11. Dialog custom quick emoji.
- Component con đáng chú ý:
  - `ChatTopBar`
  - `ChatInputBar`
  - `SwipeableReplyBubble`
  - `ChatBubble`
  - `QuotedMessagePreview`
  - `ReplyQuoteBar`
  - `TypingIndicatorBubble`
  - `PendingMessageBubble`
  - `ExplorePlanChatCard`
- Vai trò của `ChatMessageGrouping.kt`:
  - Gom các message gần nhau theo sender/time.
  - Tạo position `SINGLE/FIRST/MIDDLE/LAST`.
  - Giúp bubble chat trông giống app chat thật.
- Vì sao:
  - Chat ở project này là collaboration hub, không chỉ là IM cơ bản.
  - Support reply, reactions, pending, typing, mini AI, rich place plan là quyết định đúng với vai trò “điều phối mọi feature”.
  - Việc parse `ExplorePlanChatCard` cho thấy chat là nơi tiêu thụ object giàu cấu trúc chứ không chỉ text thô.

## 4.33 `MapShareScreen`

- File:
  - `presentation/ui/screen/map/MapShareActivity.kt`
- Cấu trúc:
  1. `Scaffold` top bar.
  2. `AndroidView(MapView)` full-screen.
  3. `DisposableEffect` dọn map khi rời màn.
- Vì sao:
  - `Home` chỉ cần map preview.
  - Màn này là phiên bản immersive/fullscreen dành cho location sharing chuyên biệt.

## 4.34 `AnalyticsScreen`

- File:
  - `presentation/ui/screen/analytics/AnalyticsScreen.kt`
- ViewModel:
  - `AnalyticsViewModel`
- Cấu trúc:
  1. `Scaffold`
  2. loading shimmer hoặc `LazyColumn`
  3. 2 card lớn:
     - `ChartCard` + `PieChart`
     - `ChartCard` + `BarChart`
- Vì sao:
  - Đây là secondary screen.
  - Thiết kế đơn giản, thiên về data inspection hơn là visual cảm xúc.
  - So với phần còn lại của app, màn này trông giống feature bổ sung hoặc đang ở giai đoạn sớm hơn.

---

## 5. Những component domain quan trọng cần hiểu riêng

## 5.1 `GoalCard`

- File:
  - `presentation/ui/screen/home/components/GoalCard.kt`
- Cấu trúc:
  - shell card chung
  - header: name + category badge
  - nhánh `SavingGoalContent`
    - amount summary
    - progress bar
    - remaining text
  - nhánh `FutureGoalContent`
    - tasks completed
    - progress bar
    - task checklist preview
- Ý nghĩa:
  - Đây là component domain-level mạnh.
  - Nó cho phép cùng một visual vocabulary biểu diễn 2 loại goal khác nhau.

## 5.2 `ContributeGoalBottomSheet`

- File:
  - `presentation/ui/screen/home/components/ContributeGoalBottomSheet.kt`
- Cấu trúc:
  - chọn goal
  - chọn payment method
  - nhập amount
  - nhập note
  - confirm
  - optional `GoalPickerSheet`
- Vì sao bottom sheet:
  - Góp tiền là action phụ từ dashboard.
  - Không cần chiếm một route full-screen riêng.

## 5.3 `PlaceCard` / `TrendingPlaceCard`

- File:
  - `presentation/ui/components/place/PlaceCards.kt`
- Cấu trúc:
  - ảnh full card
  - gradient overlay
  - tag pill
  - rating pill
  - title + location + open hours
- Ý nghĩa:
  - Explore phụ thuộc nhiều vào hình ảnh và scan nhanh.
  - Card kiểu ảnh lớn là lựa chọn đúng cho feature discovery.

---

## 6. Nhận xét thiết kế tổng thể của Android app

## 6.1 Hướng thiết kế sản phẩm

Android app này không được thiết kế như:
- một ví điện tử thuần
- một app chat thuần
- một app gallery thuần

Nó được thiết kế như một “shared operating system for couples”, nên:
- `Home` là dashboard quan hệ
- `Wallet` là dashboard tài chính
- `Explore` là planning + discovery
- `Memories` là cảm xúc và lưu giữ
- `Chat` là nơi phối hợp
- `Profile/Settings` là identity + preference

## 6.2 Hướng thiết kế UI

UI đang theo 3 tầng rõ:
- Tầng shell: `HomeScaffold`, top bar, bottom nav, background system.
- Tầng feature screen: `Home`, `Explore`, `Wallet`, `Memories`, `Profile`, `Chat`.
- Tầng reusable block: card, chip, sheet, section, goal card, place card, auth surface.

Đây là cấu trúc tốt vì:
- dễ bảo trì
- dễ scale feature
- giảm duplication UI
- vẫn cho từng màn giữ được cá tính riêng

## 6.3 Điểm mạnh đáng chú ý

- Shell app rõ ràng.
- Dùng Compose khá nhất quán.
- Tách overview screen và focused flow đúng chỗ.
- Rich card cho chat/explore rất có chủ đích sản phẩm.
- Reuse component tốt ở các domain như goal, wallet, place.

## 6.4 Dấu hiệu technical debt hoặc refactor dở dang

- `CoupleConnectActivity.kt` còn chứa một `CoupleConnectScreen` cũ, trong khi luồng chính dùng `CoupleConnectContent` ở file khác.
- `SettingsActivity.kt` vẫn tồn tại dù settings đã có route trong `HomeScaffold`.
- `AnalyticsScreen` nhìn như feature phụ hoặc chưa được hòa chung visual system mạnh như các màn chính.
- Có sự pha trộn giữa component ở `presentation/ui/components`, `presentation/ui/component`, và `presentation/ui/screen/.../components`.
- Một số màn còn nhiều hard-coded text và có lỗi encoding.

## 6.5 Kết luận ngắn gọn

Nếu phải mô tả Android app này bằng một câu:

> Đây là một app Compose nhiều feature, lấy “không gian dùng chung của cặp đôi” làm trung tâm, rồi tổ chức màn hình theo mô hình overview dashboard + focused flow.

Chính vì vậy:
- overview screens được thiết kế giàu cảm xúc, giàu card, giàu context
- action screens được thiết kế cô đọng và task-oriented
- chat, explore, wallet không đứng độc lập mà có sự nối dữ liệu với nhau

Điểm này là ý đồ thiết kế xuyên suốt rõ nhất của toàn bộ project Android.
