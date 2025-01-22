const GetCalendarList = async (category) => {
  try {
    const response = await fetch(`/calendars?category=${category}`, {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (response.ok) {
      const calendars = await response.json();

      // ID를 키로, Title을 값으로 변환
      const calendarMap = calendars.reduce((acc, calendar) => {
        acc[calendar.id] = calendar.title;
        return acc;
      }, {});

      return calendarMap;
    } else {
      console.error("Failed to fetch calendars for category:", category);
      return {};
    }
  } catch (error) {
    console.error("Error fetching calendars:", error);
    return {};
  }
};

export default GetCalendarList;
