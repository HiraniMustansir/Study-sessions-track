export type Subject = { id: string; name: string; color: string; icon: string };
export type Session = {
  id: string;
  subjectId: string;
  startedAt: number;
  endedAt: number;
  durationMs: number;
  note?: string;
};
export type Timer = {
  subjectId: string;
  startedAt: number;
  resumedAt: number | null;
  elapsedBeforePauseMs: number;
};
export type Settings = { dailyGoalMinutes: number; notificationsEnabled: boolean; reminderHour: number };
export type StudyData = { subjects: Subject[]; sessions: Session[]; timer: Timer | null; settings: Settings };

export const colors = {
  background: '#F7F7F3', ink: '#202A36', muted: '#818A94', line: '#E6E9E7',
  orange: '#F07354', dark: '#202B38', white: '#FFFFFF', green: '#50A888',
};

export const initialData: StudyData = {
  subjects: [
    { id: 'mathematics', name: 'Mathematics', color: '#F3A57D', icon: 'calculator-outline' },
    { id: 'science', name: 'Science', color: '#82B8AB', icon: 'flask-outline' },
    { id: 'reading', name: 'Reading', color: '#9A9BCB', icon: 'book-outline' },
  ],
  sessions: [], timer: null,
  settings: { dailyGoalMinutes: 180, notificationsEnabled: false, reminderHour: 21 },
};

export const dayKey = (date: Date | number) => {
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};
export const dateFromKey = (key: string) => {
  const [year, month, day] = key.split('-').map(Number);
  return new Date(year, month - 1, day);
};
export const addDays = (date: Date, amount: number) => new Date(date.getFullYear(), date.getMonth(), date.getDate() + amount);
export const sessionDay = (session: Session) => dayKey(session.endedAt);
export const totalMs = (sessions: Session[]) => sessions.reduce((sum, s) => sum + s.durationMs, 0);
export const timerElapsed = (timer: Timer, now = Date.now()) =>
  timer.elapsedBeforePauseMs + (timer.resumedAt === null ? 0 : Math.max(0, now - timer.resumedAt));
export const formatClock = (ms: number) => {
  const seconds = Math.floor(Math.max(ms, 0) / 1000);
  return `${String(Math.floor(seconds / 3600)).padStart(2, '0')}:${String(Math.floor((seconds % 3600) / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`;
};
export const formatDuration = (ms: number) => {
  const minutes = Math.round(ms / 60000);
  if (minutes < 1) return '<1 min';
  const hours = Math.floor(minutes / 60);
  return hours ? `${hours}h ${minutes % 60}m` : `${minutes}m`;
};
export const formatHours = (ms: number) => `${(ms / 3600000).toFixed(1)}h`;
export const uid = () => `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
export const getStreak = (sessions: Session[], today = new Date()) => {
  const studied = new Set(sessions.map(sessionDay));
  let cursor = studied.has(dayKey(today)) ? today : addDays(today, -1);
  let count = 0;
  while (studied.has(dayKey(cursor))) { count++; cursor = addDays(cursor, -1); }
  return count;
};
