import React, { useState } from 'react';
import { StyleSheet, Text, Pressable, View } from 'react-native';
import { EmptyState, Icon, Page, SectionHeading, StatCard, SubjectBadge } from '../components/ui';
import { useStudy } from '../lib/store';
import { addDays, colors, dayKey, formatDuration, formatHours, getStreak, sessionDay, totalMs } from '../lib/study';

type Period = 'week' | 'month';
export default function InsightsScreen() {
  const { data } = useStudy();
  const [period, setPeriod] = useState<Period>('week');
  const now = new Date();
  const periodStart = period === 'week'
    ? addDays(now, -((now.getDay() + 6) % 7))
    : new Date(now.getFullYear(), now.getMonth(), 1);
  const startKey = dayKey(periodStart);
  const sessions = data.sessions.filter(s => sessionDay(s) >= startKey && sessionDay(s) <= dayKey(now));
  const total = totalMs(sessions);
  const activeDays = new Set(sessions.map(sessionDay)).size;
  const longest = sessions.reduce((max, s) => Math.max(max, s.durationMs), 0);
  const chart = (() => {
    if (period === 'week') return Array.from({ length: 7 }, (_, i) => {
      const date = addDays(periodStart, i);
      return { label: date.toLocaleDateString([], { weekday: 'short' }).slice(0, 2), ms: totalMs(sessions.filter(s => sessionDay(s) === dayKey(date))), current: dayKey(date) === dayKey(now) };
    });
    const days = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate();
    return Array.from({ length: Math.ceil(days / 7) }, (_, i) => {
      const from = new Date(now.getFullYear(), now.getMonth(), i * 7 + 1);
      const to = new Date(now.getFullYear(), now.getMonth(), Math.min((i + 1) * 7, days));
      return { label: `W${i + 1}`, ms: totalMs(sessions.filter(s => sessionDay(s) >= dayKey(from) && sessionDay(s) <= dayKey(to))), current: dayKey(now) >= dayKey(from) && dayKey(now) <= dayKey(to) };
    });
  })();
  const chartMax = Math.max(...chart.map(x => x.ms), 60 * 60000);
  const subjectTotals = data.subjects.map(subject => ({ subject, ms: totalMs(sessions.filter(s => s.subjectId === subject.id)) })).filter(x => x.ms > 0).sort((a, b) => b.ms - a.ms);
  return <Page><Text style={styles.eyebrow}>THE BIG PICTURE</Text><Text style={styles.title}>Your insights</Text><Text style={styles.subtitle}>See how your focus adds up.</Text>
    <View style={styles.switch}><Pressable onPress={() => setPeriod('week')} style={[styles.switchItem, period === 'week' && styles.activeSwitch]}><Text style={[styles.switchText, period === 'week' && styles.activeSwitchText]}>This week</Text></Pressable><Pressable onPress={() => setPeriod('month')} style={[styles.switchItem, period === 'month' && styles.activeSwitch]}><Text style={[styles.switchText, period === 'month' && styles.activeSwitchText]}>This month</Text></Pressable></View>
    <View style={styles.hero}><View style={styles.heroIcon}><Icon name="trending-up" size={22} color="#F1AD8C" /></View><Text style={styles.heroLabel}>TOTAL FOCUS TIME</Text><Text style={styles.heroValue}>{formatHours(total)}</Text><Text style={styles.heroSub}>Across {sessions.length} {sessions.length === 1 ? 'session' : 'sessions'} · {activeDays} active {activeDays === 1 ? 'day' : 'days'}</Text></View>
    <View style={styles.statGrid}><StatCard icon="trophy-outline" label="Longest session" value={formatDuration(longest)} tint="#FFF0E8" /><View style={{ width: 10 }} /><StatCard icon="analytics-outline" label="Daily average" value={formatDuration(activeDays ? total / activeDays : 0)} tint="#E8F2ED" /></View>
    <View style={styles.chartCard}><SectionHeading title={period === 'week' ? 'Daily activity' : 'Weekly activity'} /><View style={styles.chart}>{chart.map((entry, i) => <View key={i} style={styles.barColumn}><Text style={styles.barValue}>{entry.ms ? formatHours(entry.ms) : ''}</Text><View style={styles.barTrack}><View style={[styles.bar, { height: `${Math.max(entry.ms ? 7 : 0, entry.ms / chartMax * 100)}%`, backgroundColor: entry.current ? colors.orange : '#EAB69D' }]} /></View><Text style={[styles.barLabel, entry.current && { color: colors.orange, fontWeight: '800' }]}>{entry.label}</Text></View>)}</View></View>
    <View style={styles.subjectSection}><SectionHeading title="Time by subject" />{subjectTotals.length ? subjectTotals.map(({ subject, ms }) => <View key={subject.id} style={styles.subjectRow}><SubjectBadge subject={subject} size={38} /><View style={{ flex: 1, marginLeft: 11 }}><View style={styles.subjectTextRow}><Text style={styles.subjectName}>{subject.name}</Text><Text style={styles.subjectTime}>{formatDuration(ms)}</Text></View><View style={styles.subjectTrack}><View style={[styles.subjectFill, { width: `${ms / total * 100}%`, backgroundColor: subject.color }]} /></View></View></View>) : <EmptyState icon="bar-chart-outline" title="Insights are on their way" message="Finish a study session to see your patterns here." />}</View>
    <View style={styles.streak}><View style={styles.streakIcon}><Icon name="flame" color={colors.orange} size={24} /></View><View><Text style={styles.streakTitle}>{getStreak(data.sessions)} day current streak</Text><Text style={styles.streakSub}>Every session is a step forward.</Text></View></View>
  </Page>;
}
const styles = StyleSheet.create({
  eyebrow: { color: colors.orange, fontSize: 10, fontWeight: '800', letterSpacing: 2, marginBottom: 7 }, title: { color: colors.ink, fontSize: 29, fontWeight: '900', letterSpacing: -1 }, subtitle: { color: colors.muted, fontSize: 13, marginTop: 4, marginBottom: 21 },
  switch: { backgroundColor: '#EDEFEA', borderRadius: 15, padding: 4, flexDirection: 'row', marginBottom: 17 }, switchItem: { flex: 1, height: 36, alignItems: 'center', justifyContent: 'center', borderRadius: 11 }, activeSwitch: { backgroundColor: colors.white }, switchText: { fontSize: 12, color: colors.muted, fontWeight: '700' }, activeSwitchText: { color: colors.ink },
  hero: { backgroundColor: colors.dark, borderRadius: 24, padding: 22, marginBottom: 12 }, heroIcon: { width: 40, height: 40, borderRadius: 13, backgroundColor: '#344451', alignItems: 'center', justifyContent: 'center', marginBottom: 19 }, heroLabel: { color: '#B2BEC7', fontSize: 10, fontWeight: '800', letterSpacing: 1.7 }, heroValue: { color: colors.white, fontSize: 44, fontWeight: '900', letterSpacing: -1, marginTop: 4 }, heroSub: { color: '#B2BEC7', fontSize: 12, marginTop: 2 }, statGrid: { flexDirection: 'row' },
  chartCard: { backgroundColor: colors.white, borderRadius: 23, padding: 18, marginTop: 12, borderWidth: 1, borderColor: '#F0F0ED' }, chart: { flexDirection: 'row', height: 190, alignItems: 'flex-end', gap: 7, marginTop: 6 }, barColumn: { flex: 1, alignItems: 'center', height: '100%' }, barValue: { height: 22, color: colors.muted, fontSize: 10, fontWeight: '700' }, barTrack: { flex: 1, justifyContent: 'flex-end', width: '100%', maxWidth: 31, backgroundColor: '#F6F5F1', borderRadius: 8, overflow: 'hidden' }, bar: { width: '100%', borderRadius: 8 }, barLabel: { color: colors.muted, fontSize: 10, fontWeight: '700', marginTop: 8 },
  subjectSection: { marginTop: 27 }, subjectRow: { flexDirection: 'row', alignItems: 'center', backgroundColor: colors.white, borderRadius: 17, padding: 12, marginBottom: 8 }, subjectTextRow: { flexDirection: 'row', justifyContent: 'space-between' }, subjectName: { color: colors.ink, fontWeight: '700', fontSize: 12 }, subjectTime: { color: colors.muted, fontSize: 11, fontWeight: '700' }, subjectTrack: { height: 6, borderRadius: 4, backgroundColor: '#F2F3F0', marginTop: 9, overflow: 'hidden' }, subjectFill: { height: 6, borderRadius: 4 }, streak: { marginTop: 17, backgroundColor: '#FFF0E6', borderRadius: 18, padding: 15, flexDirection: 'row', gap: 12, alignItems: 'center' }, streakIcon: { width: 42, height: 42, backgroundColor: '#FFE2CE', borderRadius: 14, alignItems: 'center', justifyContent: 'center' }, streakTitle: { color: colors.ink, fontWeight: '800', fontSize: 13 }, streakSub: { color: '#9B7D68', fontSize: 11, marginTop: 3 },
});
