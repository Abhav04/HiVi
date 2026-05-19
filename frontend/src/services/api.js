import axios from 'axios';
import { getApiUrl } from '../utils/auth';

const API = axios.create({
  baseURL: getApiUrl(),
});

export default API;