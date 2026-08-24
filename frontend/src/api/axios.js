import axios from 'axios';

// Cấu hình base URL chọc vào cổng 8080 của Spring Boot
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor Can thiệp trước khi GỬI request
axiosInstance.interceptors.request.use(
  (config) => {
    // Lôi access token từ localStorage ra
    const token = localStorage.getItem('accessToken');
    if (token) {
      // Nếu có token thì nhét vào header Authorization
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor Can thiệp sau khi NHẬN response
axiosInstance.interceptors.response.use(
  (response) => {
    // Không lỗi lầm gì thì cho qua
    return response;
  },
  (error) => {
    // Nếu Spring Boot đá về lỗi 401 (Hết hạn hoặc sai token)
    // CỰC KỲ QUAN TRỌNG: Không được chuyển hướng nếu đó là lỗi do báo Sai Mật Khẩu từ login
    const originalRequest = error.config;
    if (error.response && error.response.status === 401 && originalRequest.url !== '/auth/login') {
      // Tự động clear localStorage, đá văng về trang đăng nhập
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
