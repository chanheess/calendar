import axios from 'utils/axiosInstance';

const GetCalendarList = async (category) => {
  try {
    const response = await axios.get(`/calendars`, {
      params: { category }
    });

    const calendars = response.data;
    const calendarMap = calendars.reduce((acc, calendar) => {
      acc[calendar.id] = { 
        title: calendar.title, 
        color: calendar.color, 
        category: calendar.category,
        isSelected: calendar.checked !== false,
        fileAuthority: calendar.fileAuthority
      };
      return acc;
    }, {});

    return calendarMap;
  } catch (error) {
    console.error("Error fetching calendars for category:", category, error);
    return {};
  }
};

export default GetCalendarList;
