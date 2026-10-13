import React, { useState } from 'react';
import { StyleSheet, Text, Pressable, View } from 'react-native';
import { EmptyState, Icon, Page, SectionHeading, SessionRow } from '../components/ui';
import { useStudy } from '../lib/store';
import { colors, dateFromKey, dayKey, formatDuration, sessionDay, totalMs } from '../lib/study';

const weekdays = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
export default function CalendarScreen() {
  const { data } = useStudy();
  const [month, setMonth] = useState(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1));
  const [selected, setSelected] = useState(dayKey(new Date()));
  const byDay = (() => {
    const map = new Map<string, typeof data.sessions>();
    data.sessions.forEach(s => { const key = sessionDay(s); map.set(key, [...(map.get(key) ?? []), s]); });
    return map;
  })();
  const firstWeekday = month.getDay();
  const daysInMonth = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate();
  const cells = [...Array(firstWeekday).fill(null), ...Array.from({ length: daysInMonth }, (_, i) => i + 1)];
  while (cells.length % 7) cells.push(null);
  const selectedSessions = byDay.get(selected) ?? [];
  const monthPrefix = `${month.getFullYear()}-${String(month.getMonth() + 1).padStart(2, '0')}`;
  const monthSessions = data.sessions.filter(s => sessionDay(s).startsWith(monthPrefix));
  const daysStudied = [...byDay.keys()].filter(key => key.startsWith(monthPrefix)).length;
  const selectedDate = dateFromKey(selected);
  const changeMonth = (delta: number) => {
    const next = new Date(month.getFullYear(), month.getMonth() + delta, 1);
    setMonth(next);
    setSelected(dayKey(new Date(next.getFullYear(), next.getMonth(), 1)));
  };
  return <Page><Text style={styles.eyebrow}>YOUR CONSISTENCY</Text><Text style={styles.title}>Study calendar</Text><Text style={styles.subtitle}>A little progress, every day.</Text>
    <View style={styles.summary}><View><Text style={styles.summaryValue}>{formatDuration(totalMs(monthSessions))}</Text><Text style={styles.summaryLabel}>this month</Text></View><View style={styles.summaryDivider} /><View><Text style={styles.summaryValue}>{daysStudied}</Text><Text style={styles.summaryLabel}>days studied</Text></View><View style={styles.summaryArt}><Icon name="calendar" color="#F3A88F" size={34} /></View></View>
    <View style={styles.calendar}><View style={styles.monthHead}><Pressable onPress={() => changeMonth(-1)} style={styles.arrow}><Icon name="chevron-back" size={20} /></Pressable><Text style={styles.monthText}>{month.toLocaleDateString([], { month: 'long', year: 'numeric' })}</Text><Pressable onPress={() => changeMonth(1)} style={styles.arrow}><Icon name="chevron-forward" size={20} /></Pressable></View><View style={styles.grid}>{weekdays.map((w, i) => <View key={i} style={styles.weekday}><Text style={styles.weekdayText}>{w}</Text></View>)}{cells.map((day, i) => {
      if (!day) return <View key={`blank-${i}`} style={styles.cell} />;
      const key = dayKey(new Date(month.getFullYear(), month.getMonth(), day));
      const ms = totalMs(byDay.get(key) ?? []);
      const isSelected = key === selected;
      const isToday = key === dayKey(new Date());
      return <Pressable key={key} onPress={() => setSelected(key)} style={styles.cell}><View style={[styles.dayCircle, ms > 0 && { backgroundColor: ms >= 3 * 3600000 ? '#F2A580' : ms >= 3600000 ? '#F8D4BE' : '#FBE9DD' }, isSelected && { backgroundColor: colors.dark }]}><Text style={[styles.dayText, isSelected && { color: colors.white }, isToday && !isSelected && { color: colors.orange, fontWeight: '900' }]}>{day}</Text></View><View style={[styles.dayDot, ms > 0 && { backgroundColor: isSelected ? colors.dark : colors.orange }]} /></Pressable>;
    })}</View><View style={styles.legend}><Text style={styles.legendText}>Less</Text><View style={[styles.legendBox, { backgroundColor: '#FBE9DD' }]} /><View style={[styles.legendBox, { backgroundColor: '#F8D4BE' }]} /><View style={[styles.legendBox, { backgroundColor: '#F2A580' }]} /><Text style={styles.legendText}>More</Text></View></View>
    <View style={{ marginTop: 28 }}><SectionHeading title={selectedDate.toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' })} /><View style={styles.daySummary}><Icon name="time-outline" size={18} color={colors.orange} /><Text style={styles.daySummaryText}>{formatDuration(totalMs(selectedSessions))} studied</Text><View style={styles.dot} /><Text style={styles.daySummaryText}>{selectedSessions.length} {selectedSessions.length === 1 ? 'session' : 'sessions'}</Text></View>{selectedSessions.length ? selectedSessions.map(s => <SessionRow key={s.id} session={s} subject={data.subjects.find(x => x.id === s.subjectId)} />) : <EmptyState icon="calendar-outline" title="A fresh page" message="No study sessions were recorded on this day." />}</View>
  </Page>;
}
const styles = StyleSheet.create({
  eyebrow: { color: colors.orange, fontSize: 10, fontWeight: '800', letterSpacing: 2, marginBottom: 7 }, title: { fontSize: 29, color: colors.ink, fontWeight: '900', letterSpacing: -1 }, subtitle: { color: colors.muted, fontSize: 13, marginTop: 4, marginBottom: 22 },
  summary: { backgroundColor: colors.dark, borderRadius: 24, padding: 22, flexDirection: 'row', alignItems: 'center', marginBottom: 18, overflow: 'hidden' }, summaryValue: { color: colors.white, fontSize: 23, fontWeight: '900' }, summaryLabel: { color: '#BAC5CE', fontSize: 11, marginTop: 3 }, summaryDivider: { height: 36, width: 1, backgroundColor: '#56616C', marginHorizontal: 22 }, summaryArt: { position: 'absolute', right: 18, top: 20, opacity: .55 },
  calendar: { backgroundColor: colors.white, borderRadius: 24, padding: 15, borderWidth: 1, borderColor: '#F0F0ED' }, monthHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }, monthText: { fontSize: 16, color: colors.ink, fontWeight: '800' }, arrow: { width: 32, height: 32, borderRadius: 10, backgroundColor: colors.background, alignItems: 'center', justifyContent: 'center' }, grid: { flexDirection: 'row', flexWrap: 'wrap' }, weekday: { width: '14.2857%', alignItems: 'center', paddingBottom: 10 }, weekdayText: { color: '#9BA3AB', fontSize: 11, fontWeight: '800' }, cell: { width: '14.2857%', height: 48, alignItems: 'center', justifyContent: 'center' }, dayCircle: { width: 34, height: 34, borderRadius: 12, alignItems: 'center', justifyContent: 'center' }, dayText: { fontSize: 13, fontWeight: '700', color: colors.ink }, dayDot: { width: 3, height: 3, borderRadius: 2, marginTop: 2, backgroundColor: 'transparent' }, legend: { flexDirection: 'row', alignItems: 'center', justifyContent: 'flex-end', gap: 5, marginTop: 13 }, legendText: { color: colors.muted, fontSize: 10 }, legendBox: { width: 12, height: 12, borderRadius: 4 },
  daySummary: { flexDirection: 'row', alignItems: 'center', gap: 7, marginBottom: 14 }, daySummaryText: { fontSize: 12, color: colors.muted, fontWeight: '600' }, dot: { width: 3, height: 3, borderRadius: 2, backgroundColor: '#B9C0C5' },
});
