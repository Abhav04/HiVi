import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import RedditTrendsPage from './pages/RedditTrendsPage';
import CommunityFeedPage from './pages/CommunityFeedPage';
import OpportunitiesPage from './pages/OpportunitiesPage';
import CreatorProfilePage from './pages/CreatorProfilePage';
import HomeFeed from './pages/HomeFeed';
import OAuthSuccess from "./pages/OAuthSuccess";
import AuthConnecting from './pages/AuthConnecting';
import { AuthProvider } from './context/AuthContext';
import './App.css';

function App() {
  return (
    <AuthProvider>
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/auth/connecting" element={<AuthConnecting />} />
          <Route path="/oauth-success" element={<OAuthSuccess />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/community" element={<CommunityFeedPage />} />
          <Route path="/opportunities" element={<OpportunitiesPage />} />
          <Route path="/community/creator/:username" element={<CreatorProfilePage />} />
          <Route path="/reddit-trends" element={<RedditTrendsPage />} />
          <Route path="/feed" element={<HomeFeed />} />
        </Routes>
      </div>
    </Router>
    </AuthProvider>
  );
}

export default App;