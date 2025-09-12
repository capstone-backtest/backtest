import { lazy } from 'react';
import { RouteObject } from 'react-router-dom';

// Lazy load pages for code splitting
const HomePage = lazy(() => import('../../pages/HomePage'));
const BacktestPage = lazy(() => import('../../pages/BacktestPage'));
const LoginPage = lazy(() => import('../../pages/LoginPage'));
const SignupPage = lazy(() => import('../../pages/SignupPage'));
const CommunityPage = lazy(() => import('../../pages/CommunityPage'));
const PostDetailPage = lazy(() => import('../../pages/PostDetailPage'));
const ChatPage = lazy(() => import('../../pages/ChatPage'));

// Define routes configuration
export const routes: RouteObject[] = [
  {
    path: '/',
    element: <HomePage />,
  },
  {
    path: '/backtest',
    element: <BacktestPage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/signup',
    element: <SignupPage />,
  },
  {
    path: '/community',
    element: <CommunityPage />,
  },
  {
    path: '/community/:id',
    element: <PostDetailPage />,
  },
  {
    path: '/chat',
    element: <ChatPage />,
  },
];

// Route paths for type safety
export const ROUTE_PATHS = {
  HOME: '/',
  BACKTEST: '/backtest',
  LOGIN: '/login',
  SIGNUP: '/signup',
  COMMUNITY: '/community',
  CHAT: '/chat',
  COMMUNITY_POST: (id: string) => `/community/${id}`,
} as const;