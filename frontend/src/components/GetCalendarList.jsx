import axios from "axios";

const GetCalendarList = async (category) => {
  try {
    const response = await axios.get(`/calendars`, {
      params: { category }
    });

    const calendars = response.data;
    const calendarMap = calendars.reduce((acc, calendar) => {
      acc[calendar.id] = { title: calendar.title, color: calendar.color, category: calendar.category };
      return acc;
    }, {});

    return calendarMap;
  } catch (error) {
    if (error.status === 401) {
      window.location.href = "/auth/login";
    }

    console.error("Error fetching calendars for category:", category, error);
    return {};
  }
};

export default GetCalendarList;
