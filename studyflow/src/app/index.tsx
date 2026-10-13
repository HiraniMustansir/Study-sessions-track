import { LinearGradient } from 'expo-linear-gradient';
import { useRouter } from 'expo-router';
import React, { useEffect, useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { EmptyState, Icon, Page, SectionHeading, SessionRow, StatCard } from '../components/ui';
import { useStudy } from '../lib/store';
import { colors, dayKey, formatClock, formatDuration, getStreak, sessionDay, timerElapsed, totalMs } from '../lib/study';

export default function TodayScreen() {
  const { data, loaded, startTimer, pauseTimer, resumeTimer, finishTimer, discardTimer, deleteSession } = useStudy();
  const router = useRouter();
  const [selected, setSelected] = useState(data.subjects[0]?.id);
  const [now, setNow] = useState(Date.now);
  useEffect(() => { const id = setInterval(() => setNow(Date.now()), 1000); return () => clearInterval(id); }, []);
  const selectedSubject = data.subjects.some(s => s.id === selected) ? selected : data.subjects[0]?.id;
  const today = new Date(now);
  const todaysSessions = data.sessions.filter(s => sessionDay(s) === dayKey(today));
  const elapsed = data.timer ? timerElapsed(data.timer, now) : 0;
  const todayMs = totalMs(todaysSessions) + elapsed;
  const goalMs = data.settings.dailyGoalMinutes * 60000;
  const progress = Math.min(100, Math.round(todayMs / goalMs * 100));
  const activeSubject = data.subjects.find(s => s.id === data.timer?.subjectId);
  const dateLabel = today.toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' });
  const handleDeleteSession = (id: string) => Alert.alert('Delete session?', 'This will remove it from your progress.', [
    { text: 'Cancel', style: 'cancel' }, { text: 'Delete', style: 'destructive', onPress: () => deleteSession(id) },
  ]);
  return <Page>
    <View style={styles.header}><View><Text style={styles.eyebrow}>YOUR STUDY SPACE</Text><Text style={styles.title}>Make today count.</Text><Text style={styles.date}>{dateLabel}</Text></View><View style={styles.headerIcon}><Icon name="sparkles" color={colors.orange} size={23} /></View></View>

    <LinearGradient colors={['#253545', '#1D2936']} start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }} style={styles.timerCard}>
      <View style={styles.timerTop}><View style={styles.liveDot} /><Text style={styles.timerTopText}>{data.timer ? data.timer.resumedAt === null ? 'SESSION PAUSED' : 'FOCUS SESSION IN PROGRESS' : 'READY WHEN YOU ARE'}</Text><Icon name="time-outline" size={18} color="#B8C3CD" /></View>
      <Text style={styles.timer}>{formatClock(elapsed)}</Text>
      <Text style={styles.timerHint}>{data.timer ? `Studying ${activeSubject?.name ?? 'a subject'}` : 'Settle in, choose a subject, and begin.'}</Text>
      {data.timer ? <View style={styles.timerActions}><Pressable style={styles.pauseButton} onPress={data.timer.resumedAt === null ? resumeTimer : pauseTimer}><Icon name={data.timer.resumedAt === null ? 'play' : 'pause'} size={18} color={colors.white} /><Text style={styles.pauseText}>{data.timer.resumedAt === null ? 'Resume' : 'Pause'}</Text></Pressable><Pressable style={styles.finishButton} onPress={finishTimer}><Icon name="checkmark" size={20} color={colors.ink} /><Text style={styles.finishText}>Finish session</Text></Pressable></View> : <Pressable disabled={!selectedSubject || !loaded} style={[styles.startButton, !selectedSubject && { opacity: .5 }]} onPress={() => selectedSubject && startTimer(selectedSubject)}><Icon name="play" size={18} color={colors.ink} /><Text style={styles.startText}>Start studying</Text><Icon name="arrow-forward" size={18} color={colors.ink} /></Pressable>}
      {data.timer && <Pressable style={styles.discard} onPress={() => Alert.alert('Discard this session?', 'Your current timer will be cleared without saving.', [{ text: 'Keep studying', style: 'cancel' }, { text: 'Discard', style: 'destructive', onPress: discardTimer }])}><Text style={styles.discardText}>Discard session</Text></Pressable>}
    </LinearGradient>

    {!data.timer && <View style={styles.subjectBlock}><SectionHeading title="What are you studying?" action="Manage" onAction={() => router.push('/subjects')} /><View style={styles.subjectChips}>{data.subjects.map(s => <Pressable key={s.id} onPress={() => setSelected(s.id)} style={[styles.chip, selectedSubject === s.id && styles.selectedChip]}><View style={[styles.chipDot, { backgroundColor: s.color }]} /><Text style={[styles.chipText, selectedSubject === s.id && styles.selectedChipText]}>{s.name}</Text></Pressable>)}{data.subjects.length === 0 && <Text style={styles.emptySubject}>Add a subject to start your first session.</Text>}</View></View>}

    <View style={styles.statsBlock}><SectionHeading title="Today's progress" /><View style={styles.statGrid}><StatCard icon="hourglass-outline" label="Study time" value={formatDuration(todayMs)} /><View style={{ width: 10 }} /><StatCard icon="albums-outline" label="Sessions" value={String(todaysSessions.length + (data.timer ? 1 : 0))} tint="#E7F3EF" /></View></View>
    <View style={styles.goalCard}><View style={styles.goalHead}><View style={styles.goalIcon}><Icon name="flag-outline" color={colors.orange} size={20} /></View><View style={{ flex: 1, marginLeft: 10 }}><Text style={styles.goalTitle}>Daily goal</Text><Text style={styles.goalSub}>{formatDuration(todayMs)} of {formatDuration(goalMs)}</Text></View><Text style={styles.goalPercent}>{progress}%</Text></View><View style={styles.progressTrack}><View style={[styles.progressFill, { width: `${progress}%` }]} /></View></View>
    <View style={styles.streakCard}><Icon name="flame" color={colors.orange} size={19} /><Text style={styles.streakText}><Text style={{ fontWeight: '800' }}>{getStreak(data.sessions)} day streak</Text> · Keep showing up for yourself</Text></View>
    <View style={styles.sessionsBlock}><SectionHeading title="Recent sessions" action="View calendar" onAction={() => router.push('/calendar')} />{data.sessions.slice(0, 4).length ? data.sessions.slice(0, 4).map(s => <SessionRow key={s.id} session={s} subject={data.subjects.find(x => x.id === s.subjectId)} onLongPress={() => handleDeleteSession(s.id)} />) : <EmptyState icon="book-outline" title="No sessions yet" message="Start your first study session and your progress will appear here." />}</View>
  </Page>;
}
const styles = StyleSheet.create({
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 23 }, eyebrow: { fontSize: 10, letterSpacing: 2, fontWeight: '800', color: colors.orange, marginBottom: 7 }, title: { color: colors.ink, fontWeight: '900', fontSize: 28, letterSpacing: -1.2 }, date: { fontSize: 13, color: colors.muted, marginTop: 5 }, headerIcon: { width: 42, height: 42, borderRadius: 15, backgroundColor: '#FFF0EA', alignItems: 'center', justifyContent: 'center' },
  timerCard: { borderRadius: 27, padding: 23, overflow: 'hidden' }, timerTop: { flexDirection: 'row', alignItems: 'center' }, liveDot: { width: 7, height: 7, borderRadius: 4, backgroundColor: '#F7A689', marginRight: 8 }, timerTopText: { color: '#B8C3CD', fontSize: 10, fontWeight: '800', letterSpacing: 1.4, flex: 1 }, timer: { fontSize: 49, fontWeight: '800', color: colors.white, letterSpacing: 1.5, textAlign: 'center', marginTop: 34, fontVariant: ['tabular-nums'] }, timerHint: { color: '#B8C3CD', textAlign: 'center', fontSize: 13, marginTop: 6, marginBottom: 31 },
  startButton: { backgroundColor: '#F5AE89', minHeight: 53, borderRadius: 16, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 18 }, startText: { color: colors.ink, fontWeight: '800', fontSize: 15 }, timerActions: { flexDirection: 'row', gap: 9 }, pauseButton: { borderWidth: 1, borderColor: '#6C7986', borderRadius: 16, minHeight: 53, paddingHorizontal: 18, flexDirection: 'row', gap: 6, alignItems: 'center' }, pauseText: { color: colors.white, fontWeight: '700' }, finishButton: { flex: 1, backgroundColor: '#F5AE89', borderRadius: 16, minHeight: 53, flexDirection: 'row', gap: 7, alignItems: 'center', justifyContent: 'center' }, finishText: { color: colors.ink, fontWeight: '800' }, discard: { alignSelf: 'center', marginTop: 16 }, discardText: { color: '#BAC4CE', fontSize: 11, textDecorationLine: 'underline' },
  subjectBlock: { marginTop: 26 }, subjectChips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 }, chip: { flexDirection: 'row', alignItems: 'center', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 10, borderColor: colors.line, borderWidth: 1, backgroundColor: colors.white }, selectedChip: { backgroundColor: '#FFF1EA', borderColor: '#F4B597' }, chipDot: { width: 9, height: 9, borderRadius: 5, marginRight: 7 }, chipText: { color: '#6B747E', fontSize: 12, fontWeight: '700' }, selectedChipText: { color: colors.ink }, emptySubject: { color: colors.muted, fontSize: 12 },
  statsBlock: { marginTop: 30 }, statGrid: { flexDirection: 'row' }, goalCard: { backgroundColor: colors.white, borderRadius: 20, padding: 17, marginTop: 11, borderWidth: 1, borderColor: '#F0F0ED' }, goalHead: { flexDirection: 'row', alignItems: 'center', marginBottom: 15 }, goalIcon: { width: 36, height: 36, borderRadius: 12, backgroundColor: '#FFF0EA', alignItems: 'center', justifyContent: 'center' }, goalTitle: { fontSize: 13, color: colors.ink, fontWeight: '800' }, goalSub: { color: colors.muted, fontSize: 11, marginTop: 3 }, goalPercent: { color: colors.orange, fontWeight: '900', fontSize: 18 }, progressTrack: { height: 8, borderRadius: 5, backgroundColor: '#F1F2EF', overflow: 'hidden' }, progressFill: { height: 8, backgroundColor: colors.orange, borderRadius: 5 }, streakCard: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#FFF2E7', padding: 14, borderRadius: 16, marginTop: 11, gap: 8 }, streakText: { color: '#7D5D49', fontSize: 12, flex: 1 }, sessionsBlock: { marginTop: 29 },
});
