<!DOCTYPE html>
<html lang='en'>
<head>
    <meta charset='utf-8'/>
    <title>chcalendar</title>
    <script src='https://cdn.jsdelivr.net/npm/fullcalendar@6.1.15/index.global.min.js'></script>
    <link rel="stylesheet" href="/css/style.css">

    <!-- 브라우저가 favicon.ico를 요청하지 않도록 -->
    <link rel="icon" href="data:,">
    <style>
        /* 캘린더 스타일 */
        #calendar {
            max-width: 800px;
            margin: 20px auto;
            padding: 10px;
            border: 1px solid #ddd;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            background-color: #f9f9f9;
        }

        /* 캘린더 일자 hover 스타일 */
        .fc-daygrid-day:hover {
            background-color: #e6f7ff;
            cursor: pointer;
        }

        /* 회색 반투명 배경 */
        .dimmed-background {
            background-color: rgba(0, 0, 0, 0.5);
            pointer-events: none;
        }

        #userNickname {
            display: flex;
            align-items: center;
        }

        /* 팝업 오버레이 */
        .popup-overlay {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.2);
            z-index: 999;
        }

        /* 팝업 스타일 */
        .popup {
            display: none;
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 330px;
            background-color: white;
            z-index: 1000;
            border-radius: 8px;
            max-height: 90vh;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
            flex-direction: column;
            overflow: hidden;
        }

        .popup-overlay.show {
            display: block;
        }

        .popup.show {
            display: block;
        }

        /* 팝업 버튼 스타일 */
        .popup-close,
        .popup-edit-event,
        .popup-new-event,
        .popup-back,
        .popup-delete,
        .popup-save {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 10px;
            border-radius: 8px;
            cursor: pointer;
            margin-top: 5px;
        }

        .popup-add-notification {
            background-color: grey;
            margin-top: 0px;
        }

        .popup-close {
            background-color: #f44336;
            margin-left: 10px;
        }

        .popup-delete,
        .popup-back {
            margin-left: 10px;
            background-color: #ff9800;
        }

        /* 팝업 헤더 스타일 */
        .popup-header {
            padding: 10px;
            font-size: 20px
            font-weight: bold;
            background-color: #f5f5f5;
            border-bottom: 1px solid #ddd;
            text-align: center;
            margin-bottom: 10px;
        }

        /* 팝업 내용 스타일 */
        .popup-content {
            overflow-y: auto;
            overflow-x: hidden;
            margin-bottom: 20px;
            flex: 1;
            max-height: 500px;
            padding: 20px;
            box-sizing: border-box;
        }

        /* 팝업 푸터 (버튼 고정) */
        .popup-footer {
            padding: 10px;
            background-color: #f5f5f5;
            border-top: 1px solid #ddd;
            display: flex;
            justify-content: space-between;
            position: sticky;
            bottom: 0;
            gap: 10px;
        }

        .hidden {
            display: none;
        }

        .fc-toolbar-chunk {
            text-align: center;            /* 내용 중앙 정렬 */
            white-space: nowrap;           /* 텍스트 줄바꿈 방지 */
        }

        .fc-today-button {
            max-width: 80px;
        }

        .X-button {
            width: 20px;
            height: 20px;
            padding: 2px;
            font-size: 12px;
            line-height: 1;
            border-radius: 50%; /* 원형 버튼 */
            background-color: grey;
        }

        .notification-row {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 5px;
        }

        .notification-unit,
        .notification-time {
            flex: 1;
            padding: 5px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }

        .schedule-repeat-section {
            flex-direction: column;
            gap: 10px;
        }

        .repeat-row {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .repeat-interval,
        .repeat-type,
        .repeat-end {
            flex: 1;
            padding: 5px;
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 4px;
            margin-bottom: 10px;
        }

        .schedule-information {
            display: flex;
            flex-direction: column;
            gap: 10px; /* 요소 간 간격 조정 */
        }

        .schedule-information input,
        .schedule-information select,
        .schedule-information textarea {
            width: 100%;
            padding: 6px; /* 입력 필드 내부 여백 줄이기 */
            font-size: 14px;
            border: 1px solid #ccc;
            border-radius: 4px;
            box-sizing: border-box;
        }


        .modal {
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
        }

        .modal-content {
            background-color: #fefefe;
            padding: 20px;
            border: 1px solid #888;
            border-radius: 8px;
            width: 400px;
            position: relative;
        }

        .close {
            position: absolute;
            top: 10px;
            right: 15px;
            color: #aaa;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
        }

        .close:hover,
        .close:focus {
            color: black;
            text-decoration: none;
            cursor: pointer;
        }

        .user-list-container {
            height: 150px;
            overflow-y: auto;
            border: 1px solid #ccc;
            padding: 10px;
            border-radius: 4px;
            background-color: #f9f9f9;
        }

        .user-item {
            margin-bottom: 5px;
            padding: 5px;
            border-bottom: 1px solid #ddd;
        }


    </style>
</head>
<script>
    document.addEventListener('DOMContentLoaded', async function() {


        var calendarEl = document.getElementById('calendar');
        var popup = document.getElementById('popup');
        var popupOverlay = document.getElementById('popup-overlay');
        var popupContent = document.getElementById('popup-content');
        var popupHeader = document.getElementById('popup-header');
        var popupClose = document.getElementById('popup-close');
        var popupEditEvent = document.getElementById('popup-edit-event');
        var popupNewEvent = document.getElementById('popup-new-event');

        var scheduleFormPopup = document.getElementById('schedule-form-popup');
        var scheduleFormHeader = document.getElementById('schedule-form-header');
        var scheduleFormSave = document.getElementById('schedule-form-save');
        var scheduleFormClose = document.getElementById('schedule-form-close');

        // DOM 요소 변수 선언
        var scheduleTitle = document.getElementById('schedule-title');
        var scheduleDescription = document.getElementById('schedule-description');
        var scheduleStart = document.getElementById('schedule-start');
        var scheduleEnd = document.getElementById('schedule-end');

        var scheduleRepeatInterval = document.getElementById('schedule-repeat-interval');
        var scheduleRepeatType = document.getElementById('schedule-repeat-type');
        var scheduleRepeatEnd = document.getElementById('schedule-repeat-end');

        var scheduleRepeatSection = document.getElementById('schedule-repeat-section');
        var scheduleRepeatCheckbox = document.getElementById('schedule-repeat-checkbox');

        var scheduleNotificationSection = document.getElementById('schedule-notification-section');
        var scheduleNotificationCheckbox = document.getElementById('schedule-notification-checkbox');
        var scheduleNotificationList = document.getElementById('schedule-notification-list');
        var scheduleAddNotification = document.getElementById('schedule-add-notification');

        var choicePopup = document.getElementById('repeat-choice-popup');
        var choicePopupClose = document.getElementById('repeat-choice-popup-close');
        var updateSingleEvent = document.getElementById('update-single-event');
        var updateAllEvents = document.getElementById('update-all-events');

        const notificationButton = document.getElementById("notificationButton");
        const notificationDropdown = document.getElementById("notificationDropdown");
        const notificationList = document.getElementById("notificationList");
        const notificationCount = document.getElementById("notificationCount");

        // checkedState 변수 선언 및 초기화
        const checkedState = new Set();
        const calendarCache = {};

        getCalendarList('USER', 'myCalendarList'); // 개인 캘린더 불러오기
        getCalendarList('GROUP', 'groupCalendarList'); // 그룹 캘린더 불러오기

        let isEditMode = false;
        var currentScheduleId = null;
        let selectedEvent;
        let isSaving = false;   // 추가된 상태 변수로 중복 요청을 방지
        var initialScheduleData = {
                scheduleDto: {},
                notificationDto: [],
                repeatDto: null
            };   // 변경된 값만 patch 해주기 위해






        async function fetchNotifications() {
            try {
                const response = await fetch('/notifications',{
                    method: 'GET',
                    credentials: 'include',
                });
                const notifications = await response.json();

                const notificationList = document.getElementById('notificationList');
                const noNotificationsMessage = document.getElementById('noNotificationsMessage');
                const notificationCount = document.getElementById('notificationCount');

                notificationList.innerHTML = ''; // 기존 알림 초기화

                if (notifications.length === 0) {
                    noNotificationsMessage.classList.add('visible');
                    noNotificationsMessage.classList.remove('hidden');
                    notificationCount.style.display = 'none';
                } else {
                    noNotificationsMessage.classList.add('hidden');
                    noNotificationsMessage.classList.remove('visible');
                    notificationCount.style.display = 'inline-block';
                    notificationCount.textContent = notifications.length;

                    notifications.forEach(notification => {
                        const listItem = document.createElement('li');
                        const message = document.createElement('p');
                        message.textContent = notification.message;
                        listItem.appendChild(message);

                        // 수락/거절 버튼이 필요한 경우
                        if (notification.type === 'INVITE') {
                            const buttonContainer = document.createElement('div'); // 버튼 컨테이너 생성
                            buttonContainer.className = 'button-container'; // 스타일 적용

                            const acceptButton = document.createElement('button');
                            acceptButton.textContent = '수락';
                            acceptButton.className = 'accept-button';
                            acceptButton.addEventListener('click', () => handleNotificationAccept(notification, true));

                            const declineButton = document.createElement('button');
                            declineButton.textContent = '거절';
                            declineButton.className = 'decline-button';
                            declineButton.addEventListener('click', () => handleNotificationReject(notification, false));

                            // 버튼을 컨테이너에 추가
                            buttonContainer.appendChild(acceptButton);
                            buttonContainer.appendChild(declineButton);

                            // 버튼 컨테이너를 리스트 아이템에 추가
                            listItem.appendChild(buttonContainer);
                        }

                        notificationList.appendChild(listItem);
                    });
                }
            } catch (error) {
                console.error("Error fetching notifications:", error);
            }
        }

        async function handleNotificationAccept(notification) {
            try {
                const response = await fetch(`/notifications/accept`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(notification),
                });

                if (response.ok) {
                    fetchNotifications(); // 알림 목록 갱신
                    //TODO: 성공했다는 피드백 필요할듯
                }
            } catch (error) {
                console.error(`Error processing notification ${notificationId}:`, error);
            }
        }

        async function handleNotificationReject(notification) {
            try {
                const response = await fetch(`/notifications/reject`, {
                    method: 'DELETE',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(notification),
                });

                if (response.ok) {
                    fetchNotifications(); // 알림 목록 갱신
                    //TODO: 거절했다는 피드백 필요할듯
                }
            } catch (error) {
                console.error(`Error processing notification ${notificationId}:`, error);
            }
        }

        document.getElementById('userProfileButton').addEventListener('click', getUserProfile);

        popupClose.addEventListener('click', function() {
            popup.classList.remove('show');
            popupOverlay.classList.remove('show');

            // 버튼 원래대로 변경
            popupEditEvent.classList.add('hidden');
            popupNewEvent.classList.remove('hidden');
        });

        popupOverlay.addEventListener('click', function() {
            popup.classList.remove('show');
            popupOverlay.classList.remove('show');
            scheduleFormPopup.classList.remove('show');

            // 버튼 원래대로 변경
            popupEditEvent.classList.add('hidden');
            popupNewEvent.classList.remove('hidden');
        });

        popupEditEvent.addEventListener('click', function() {
            popup.classList.remove('show');
            scheduleFormPopup.classList.add('show');
            openEditScheduleForm();
        });

        popupNewEvent.addEventListener('click', function() {
            popup.classList.remove('show');
            scheduleFormPopup.classList.add('show');
            openCreateScheduleForm();
        });

        scheduleFormClose.addEventListener('click', function() {
            scheduleFormPopup.classList.remove('show');
            popupOverlay.classList.remove('show');

            // 버튼 원래대로 변경
            popupEditEvent.classList.add('hidden');
            popupNewEvent.classList.remove('hidden');
        });

        // Show/hide repeat options based on checkbox state
        scheduleRepeatCheckbox.addEventListener('change', function () {
            if (this.checked) {
                scheduleRepeatSection.classList.remove('hidden');
            } else {
                scheduleRepeatSection.classList.add('hidden');
            }
        });
        scheduleNotificationCheckbox.addEventListener('change', function() {
            if (this.checked) {
                scheduleNotificationSection.classList.remove('hidden');
            } else {
                scheduleNotificationSection.classList.add('hidden');
            }
        });

        scheduleAddNotification.addEventListener('click', function() {
            addNotificationRow();
        });


        notificationButton.addEventListener("click", () => {
            if (notificationDropdown.style.display === "none" || !notificationDropdown.style.display) {
                notificationDropdown.style.display = "block";
            } else {
                notificationDropdown.style.display = "none";
            }
        });

        // 클릭한 곳이 알림창 외부라면 닫기
        document.addEventListener("click", (event) => {
            if (!notificationButton.contains(event.target) && !notificationDropdown.contains(event.target)) {
                notificationDropdown.style.display = "none";
            }
        });

        document.querySelectorAll(".toggle-section").forEach(toggle => {
            toggle.addEventListener("click", () => {
                const target = document.getElementById(toggle.dataset.target);
                const toggleIcon = toggle.querySelector(".toggle-icon");

                if (target.classList.contains("hidden")) {
                    target.classList.remove("hidden");
                    toggleIcon.textContent = "▼"; // 열림 상태
                } else {
                    target.classList.add("hidden");
                    toggleIcon.textContent = "▲"; // 닫힘 상태
                }
            });
        });

        // 모달 요소
        const modal = document.getElementById('createCalendarModal');
        const addCalendarButton = document.getElementById('addCalendarButton');
        const closeModalButton = document.getElementById('closeModalButton');

        // 모달 열기
        addCalendarButton.addEventListener('click', () => {
            modal.style.display = 'flex';
        });

        // 모달 닫기
        closeModalButton.addEventListener('click', () => {
            modal.style.display = 'none';
        });

        // 배경 클릭 시 닫기
        window.addEventListener('click', (event) => {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        });

        // 생성 버튼 클릭 이벤트
        const createCalendarButton = document.getElementById('createCalendarButton');
        createCalendarButton.addEventListener('click', () => {
            const title = document.getElementById('calendarTitle').value;
            const type = document.getElementById('calendarType').value;

            if (title) {
                addCalendar(title, type);
            } else {
                alert('캘린더 제목을 입력해주세요.');
            }
        });

        async function addCalendar(title, type) {
            try {
                const response = await fetch('/calendars', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        category: type,
                        title: title
                    })
                });

                if (response.ok) {
                    const calendarInfo = await response.json();
                    alert("캘린더가 추가되었습니다.");
                    modal.style.display = 'none';

                    const targetListId = type === 'USER' ? 'myCalendarList' : 'groupCalendarList';

                    addCalendarToList(type, calendarInfo, targetListId);
                } else {
                    const errorResponse = await response.json();
                    alert(`오류 발생: ${errorResponse.message || response.status}`);
                    console.error('Error Response:', errorResponse);
                }
            } catch (error) {
                console.error('Error: ', error);
                alert('캘린더 추가 중 오류가 발생했습니다.');
            }
        }

        // 특정 캘린더 ID로 캐시에서 캘린더를 가져오는 함수
        function getCalendarFromCache(calendarId) {
            return calendarCache[calendarId] || null; // 해당 ID가 없으면 null 반환
        }

        // 캘린더 셀렉트 박스 채우는 함수
        function populateCalendarSelectBox() {
            const calendarSelect = document.getElementById('schedule-calendar-select');
            calendarSelect.innerHTML = ''; // 기존 옵션 초기화

            getSelectedCalendarIds().forEach(calendarId => {
                const calendar = getCalendarFromCache(calendarId); // 캐시에서 캘린더 가져오기

                if (calendar) {
                    const option = document.createElement('option');
                    option.value = calendarId; // 키 자체가 id이므로 그대로 사용
                    option.textContent = calendar; // title은 값에 포함됨
                    calendarSelect.appendChild(option);
                }
            });
        }

        function editCalendarSelectBox() {
            const calendarSelect = document.getElementById('schedule-calendar-select');
            calendarSelect.innerHTML = ''; // 기존 옵션 초기화

            const calendar = getCalendarFromCache(initialScheduleData.scheduleDto.calendarId); // 캐시에서 캘린더 가져오기

            if (calendar) {
                const option = document.createElement('option');
                option.value = initialScheduleData.scheduleDto.calendarId;
                option.textContent = calendar;
                calendarSelect.appendChild(option);
            }
        }

        // 팝업 열고 닫기
        const manageCalendarModal = document.getElementById('manageCalendarModal');
        const closeManageCalendarModal = document.getElementById('closeManageCalendarModal');

        function openManageCalendarModal(calendarTitle, calendarId) {
            document.getElementById('calendarManageTitle').textContent = `${calendarTitle} 관리`;
            manageCalendarModal.style.display = 'flex';
            manageCalendarModal.dataset.calendarId = calendarId;

            // 유저 리스트 로드
            loadUserList(calendarId);
        }

        closeManageCalendarModal.addEventListener('click', () => {
            manageCalendarModal.style.display = 'none';
        });

        // 캘린더 초대 기능
        document.getElementById('inviteUserButton').addEventListener('click', () => {
            const userName = document.getElementById('inviteUserName').value.trim();
            const calendarId = manageCalendarModal.dataset.calendarId; // 관리 창에 설정된 calendarId 사용

            if (userName) {
                inviteUserToCalendar(calendarId, userName);
            } else {
                alert('사용자 이름을 입력해주세요.');
            }
        });

        function inviteUserToCalendar(groupId, userName) {
            fetch(`/notifications/groups/${groupId}/invite?nickname=${encodeURIComponent(userName)}`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(response => {
                if (response.ok) {
                    alert(`${userName} 초대 성공`);
                } else {
                    alert(`${userName} 초대 실패`);
                }
            })
            .catch(error => console.error('Error inviting user:', error));
        }

        // 유저 리스트 불러오기
        function loadUserList(calendarId) {
            fetch(`/groups/${calendarId}/users`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(response => response.json())
            .then(users => {
                const userListContainer = document.getElementById('userListContainer');
                userListContainer.innerHTML = ''; // 기존 리스트 초기화
                users.forEach(user => {
                    const userItem = document.createElement('div');
                    userItem.className = 'user-item';
                    userItem.textContent = user.userNickname;
                    userListContainer.appendChild(userItem);
                });
            })
            .catch(error => console.error('Error fetching user list:', error));
        }



    });
</script>
<body>
<!-- 헤더 추가 -->
<div class="header" id="header">
<!--    <a class="navbar-brand" href="https://chcalendar.site">chcalendar</a>-->
    <span id="userNickname"></span>
    <button id="userProfileButton">Profile</button>
    <div class="notification">
        <button id="notificationButton">
            <span class="icon">🔔</span>
            <span class="badge" id="notificationCount" style="display: none;">0</span>
        </button>
        <div class="dropdown" id="notificationDropdown">
            <h3>Notifications</h3>
            <ul id="notificationList">
                <!-- 알림 항목이 동적으로 추가됩니다. -->
            </ul>
            <p id="noNotificationsMessage" style="color: gray;">알림이 없습니다.</p>
        </div>
    </div>
</div>

<div class="calendar-container">
    <div class="calendar-sidebar">
        <div class="calendar-section">
            <h3 class="toggle-section" data-target="myCalendarList">
                내 캘린더 <span class="toggle-icon">▼</span>
            </h3>
            <ul id="myCalendarList" class="calendar-list">
                <!-- 동적으로 추가될 항목 -->
            </ul>
        </div>
        <div class="calendar-section">
            <h3 class="toggle-section" data-target="groupCalendarList">
                다른 캘린더 <span class="toggle-icon">▼</span>
            </h3>
            <ul id="groupCalendarList" class="calendar-list">
                <!-- 동적으로 추가될 항목 -->
            </ul>
            <button id="addCalendarButton">+</button>

        </div>
    </div>
    <div class="calendar-content">
        <!-- 캘린더 내용 -->
        <div id="calendar"></div>
    </div>
</div>

<div id="createCalendarModal" class="modal" style="display: none;">
    <div class="modal-content">
        <span id="closeModalButton" class="close">&times;</span>
        <h3>캘린더 생성</h3>
        <div class="form-row">
            <label for="calendarTitle">제목:</label>
            <input type="text" id="calendarTitle" placeholder="캘린더 제목" />
        </div>
        <div class="form-row">
            <label for="calendarType">유형:</label>
            <select id="calendarType">
                <option value="USER">개인</option>
                <option value="GROUP">그룹</option>
            </select>
        </div>
        <button id="createCalendarButton">생성</button>
    </div>
</div>

<!--그룹 관리 팝업-->
<div id="manageCalendarModal" class="modal" style="display: none;">
    <div class="modal-content">
        <span id="closeManageCalendarModal" class="close">&times;</span>
        <h3 id="calendarManageTitle" class="popup-header">그룹 관리</h3>

        <div class="form-section">
            <h4>캘린더 초대</h4>
            <div class="form-row">
                <input type="text" id="inviteUserName" class="form-input" placeholder="사용자 이름 입력" />
                <button id="inviteUserButton" class="form-button">초대</button>
            </div>
        </div>

        <div class="form-section">
            <h4>캘린더 유저</h4>
            <div id="userListContainer" class="user-list-container">
                <!-- 유저 리스트가 동적으로 추가됩니다. -->
            </div>
        </div>
    </div>
</div>

<div id='popup-overlay' class="popup-overlay"></div>
<div id='popup' class="popup">
    <div id='popup-header' class="popup-header">Events on selected date</div>
    <div id='popup-content' class="popup-content"></div>
    <div class="popup-footer">
        <button id="popup-new-event" class="popup-new-event">New Event</button>
        <button id="popup-edit-event" class="popup-new-event hidden">Edit Event</button>
        <button id="popup-close" class="popup-close">Close</button>
    </div>
</div>

<!-- 반복 수정 선택지 팝업 -->
<div id='repeat-choice-popup' class="popup hidden" style="z-index: 1001;">
    <div id='repeat-choice-popup-header' class="popup-header">Choose an Action</div>
    <div id='repeat-choice-popup-content' class="popup-content">
        <p>This event is part of a repeating series. What would you like to do?</p>
    </div>
    <div class="popup-footer">
        <button id="update-single-event" class="popup-new-event" data-mode="save-single">Update Single Event</button>
        <button id="update-all-events" class="popup-new-event" data-mode="save-all">Update All Events</button>
        <button id="repeat-choice-popup-close" class="popup-close">Cancel</button>
    </div>
</div>

<!-- 일정 작성 팝업 -->
<div id='schedule-form-popup' class="popup hidden" data-selected-date="">
    <div id='schedule-form-header' class="popup-header">Edit Schedule</div>
    <div id='schedule-form-content' class="popup-content">
        <div id='schedule-information' class="schedule-information">
            <label>Title<br><input type="text" id="schedule-title"></label>
            <label>Description<br><textarea id="schedule-description"></textarea></label>
            <label>Start Time<br><input type="datetime-local" id="schedule-start"></label>
            <label>End Time<br><input type="datetime-local" id="schedule-end"></label>
            <label>Calendar<br>
                <select id="schedule-calendar-select">
                    <!-- 동적으로 캘린더 목록이 추가될 부분 -->
                </select>
            </label><br><br>
        </div>

        <!-- 알림 설정 -->
        <label><input type="checkbox" id="schedule-notification-checkbox"> Notifications</label><br><br>
        <div id='schedule-notification-section' class="hidden">
            <div id="schedule-notification-list"></div><br>
            <button id="schedule-add-notification" class="popup-add-notification" type="submit">Add Notification</button><br><br>
        </div>

        <label><input type="checkbox" id='schedule-repeat-checkbox'> Repeat</label><br><br>
        <div id='schedule-repeat-section' class="schedule-repeat-section hidden">
            <label for="schedule-repeat-interval">Repeat Interval</label>
            <div class="repeat-row">
                <input type="number" id="schedule-repeat-interval" class="repeat-interval" min="1" value="1">
                <select id="schedule-repeat-type" class="repeat-type">
                    <option value="d">Day(s)</option>
                    <option value="w">Week(s)</option>
                    <option value="m">Month(s)</option>
                    <option value="y">Year(s)</option>
                </select>
            </div>

            <label for="schedule-repeat-end">Repeat End Time</label>
            <div class="repeat-row">
                <input type="datetime-local" id="schedule-repeat-end" class="repeat-end">
            </div>
        </div>
    </div>

    <div class="popup-footer">
        <button id="schedule-form-save" class="popup-new-event">Save</button>
        <button id="schedule-form-delete" class="popup-delete hidden">Delete</button>
        <button id="schedule-form-close" class="popup-close">Close</button>
    </div>
</div>

</body>
</html>
