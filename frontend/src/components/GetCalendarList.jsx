import axios from "axios";

const GetCalendarList = async (category) => {
  try {
    const response = await axios.get(`/calendars`, {
      params: { category }
    });

    const calendars = response.data;

    // ID를 키로, Title을 값으로 변환
    const calendarMap = calendars.reduce((acc, calendar) => {
      acc[calendar.id] = calendar.title;
      return acc;
    }, {});

    return calendarMap;
  } catch (error) {
    console.error("Error fetching calendars for category:", category, error);
    return {};
  }
};

export default GetCalendarList;
