// 手机号正则
export const phoneRegex = /^1[3-9]\d{9}$/;

// 邮箱正则
export const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

// 身份证正则 (简单校验18位)
export const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\d|3[0-1])\d{3}[\dXx]$/;

// 学号/工号正则 (数字或字母组合，4-20位)
export const studentIdRegex = /^[a-zA-Z0-9]{4,20}$/;

// 密码正则 (字母+数字，8-20位)
export const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,20}$/;

// 姓名正则 (允许中文、英文、空格、点、中间点，2-20位)
// 支持英文名（如 "John Doe"）、少数民族名（如 "阿凡提·买买提"）、生僻字（扩展A区）
export const nameRegex = /^[\u4e00-\u9fa5\u3400-\u4db5a-zA-Z\s.·]{2,20}$/;

/**
 * 校验手机号
 * @param {string} phone 
 * @returns {boolean}
 */
export const validatePhone = (phone) => {
  return phoneRegex.test(phone);
};

/**
 * 校验邮箱
 * @param {string} email 
 * @returns {boolean}
 */
export const validateEmail = (email) => {
  return emailRegex.test(email);
};

/**
 * 校验学号
 * @param {string} id 
 * @returns {boolean}
 */
export const validateStudentId = (id) => {
  return studentIdRegex.test(id);
};

/**
 * 校验密码强度
 * @param {string} password 
 * @returns {boolean}
 */
export const validatePassword = (password) => {
  return passwordRegex.test(password);
};
