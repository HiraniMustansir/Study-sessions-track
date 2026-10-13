import AsyncStorage from '@react-native-async-storage/async-storage';
import React, { createContext, useContext, useEffect, useState } from 'react';
import { Alert } from 'react-native';
import { syncDailySummary } from './notifications';
import { initialData, Session, Settings, StudyData, timerElapsed, uid } from './study';

const STORAGE_KEY = 'studyflow-data-v1';
type Store = {
  data: StudyData;
  loaded: boolean;
  startTimer: (subjectId: string) => void;
  pauseTimer: () => void;
  resumeTimer: () => void;
  finishTimer: () => void;
  discardTimer: () => void;
  addSubject: (name: string, color: string, icon: string) => void;
  deleteSubject: (id: string) => void;
  updateSettings: (settings: Partial<Settings>) => void;
  deleteSession: (id: string) => void;
};
const Context = createContext<Store | null>(null);

export function StudyProvider({ children }: { children: React.ReactNode }) {
  const [data, setData] = useState<StudyData>(initialData);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY)
      .then(raw => { if (raw) setData({ ...initialData, ...JSON.parse(raw) }); })
      .catch(() => Alert.alert('Could not load study data', 'Your saved sessions could not be read.'))
      .finally(() => setLoaded(true));
  }, []);

  useEffect(() => {
    if (!loaded) return;
    AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(data)).catch(() =>
      Alert.alert('Could not save study data', 'Please check available storage on your device.')
    );
    syncDailySummary(data).catch(() => undefined);
  }, [data, loaded]);

  const startTimer = (subjectId: string) => setData(d => {
    if (d.timer) return d;
    const now = Date.now();
    return { ...d, timer: { subjectId, startedAt: now, resumedAt: now, elapsedBeforePauseMs: 0 } };
  });
  const pauseTimer = () => setData(d => {
    if (!d.timer || d.timer.resumedAt === null) return d;
    return { ...d, timer: { ...d.timer, elapsedBeforePauseMs: timerElapsed(d.timer), resumedAt: null } };
  });
  const resumeTimer = () => setData(d => {
    if (!d.timer || d.timer.resumedAt !== null) return d;
    return { ...d, timer: { ...d.timer, resumedAt: Date.now() } };
  });
  const finishTimer = () => setData(d => {
    if (!d.timer) return d;
    const now = Date.now();
    const durationMs = timerElapsed(d.timer, now);
    const session: Session = { id: uid(), subjectId: d.timer.subjectId, startedAt: d.timer.startedAt, endedAt: now, durationMs };
    return { ...d, timer: null, sessions: durationMs >= 1000 ? [session, ...d.sessions] : d.sessions };
  });
  const discardTimer = () => setData(d => ({ ...d, timer: null }));
  const addSubject = (name: string, color: string, icon: string) => setData(d => ({
    ...d, subjects: [...d.subjects, { id: uid(), name: name.trim(), color, icon }],
  }));
  const deleteSubject = (id: string) => setData(d => ({ ...d, subjects: d.subjects.filter(s => s.id !== id) }));
  const updateSettings = (settings: Partial<Settings>) => setData(d => ({ ...d, settings: { ...d.settings, ...settings } }));
  const deleteSession = (id: string) => setData(d => ({ ...d, sessions: d.sessions.filter(s => s.id !== id) }));

  return <Context.Provider value={{ data, loaded, startTimer, pauseTimer, resumeTimer, finishTimer, discardTimer, addSubject, deleteSubject, updateSettings, deleteSession }}>{children}</Context.Provider>;
}

export function useStudy() {
  const store = useContext(Context);
  if (!store) throw new Error('useStudy must be used inside StudyProvider');
  return store;
}
