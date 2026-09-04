'use client'

import { useMemo, useState } from 'react'
import type { Achievement, AchievementFrame } from '@/types/minecraft'
import { Card, CardHeader, CardTitle } from '@/components/ui/Card'
import { Badge } from '@/components/ui/Badge'

type Props = {
  achievements: Achievement[]
  categories: string[]
  globalStats?: {
    totalAchievements: number
    totalCompletions: number
    trackedPlayers: number
  }
}

type SortOption = 'default' | 'most-completed' | 'rarest' | 'alphabetical'

export default function AchievementsView({
  achievements,
  categories,
  globalStats,
}: Props) {
  const [selectedCategory, setSelectedCategory] = useState<string>('all')
  const [selectedFrame, setSelectedFrame] = useState<string>('all')
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [sortBy, setSortBy] = useState<SortOption>('default')

  const availableCategories = useMemo(() => {
    const list = [{ id: 'all', label: 'All Categories' }]
    const added = new Set<string>()

    // Priority order
    const priority = ['story', 'nether', 'end', 'adventure', 'husbandry']
    for (const key of priority) {
      if (
        categories.includes(key) ||
        achievements.some(a => a.category === key)
      ) {
        list.push({
          id: key,
          label:
            key === 'story'
              ? 'Story'
              : key === 'nether'
                ? 'Nether'
                : key === 'end'
                  ? 'The End'
                  : key === 'adventure'
                    ? 'Adventure'
                    : 'Husbandry',
        })
        added.add(key)
      }
    }

    for (const c of categories) {
      if (!added.has(c)) {
        list.push({
          id: c,
          label:
            c.charAt(0).toUpperCase() + c.slice(1).replace(/_/g, ' '),
        })
        added.add(c)
      }
    }

    for (const a of achievements) {
      if (a.category && !added.has(a.category)) {
        list.push({
          id: a.category,
          label: a.categoryName || a.category,
        })
        added.add(a.category)
      }
    }

    return list
  }, [categories, achievements])

  const statsCounts = useMemo(() => {
    const tasks = achievements.filter(a => a.frame === 'TASK').length
    const goals = achievements.filter(a => a.frame === 'GOAL').length
    const challenges = achievements.filter(a => a.frame === 'CHALLENGE').length
    return { tasks, goals, challenges }
  }, [achievements])

  const filteredAndSortedAchievements = useMemo(() => {
    let result = achievements.filter(a => {
      if (selectedCategory !== 'all' && a.category !== selectedCategory) {
        return false
      }
      if (selectedFrame !== 'all' && a.frame !== selectedFrame) {
        return false
      }
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase().trim()
        const matchTitle = a.title.toLowerCase().includes(query)
        const matchDesc = a.description.toLowerCase().includes(query)
        const matchCat = (a.categoryName || a.category)
          .toLowerCase()
          .includes(query)
        return matchTitle || matchDesc || matchCat
      }
      return true
    })

    if (sortBy === 'most-completed') {
      result = [...result].sort(
        (a, b) =>
          (b.completedPercentage ?? 0) - (a.completedPercentage ?? 0) ||
          (b.completedCount ?? 0) - (a.completedCount ?? 0)
      )
    } else if (sortBy === 'rarest') {
      result = [...result].sort(
        (a, b) =>
          (a.completedPercentage ?? 0) - (b.completedPercentage ?? 0) ||
          (a.completedCount ?? 0) - (b.completedCount ?? 0)
      )
    } else if (sortBy === 'alphabetical') {
      result = [...result].sort((a, b) => a.title.localeCompare(b.title))
    }

    return result
  }, [achievements, selectedCategory, selectedFrame, searchQuery, sortBy])

  const getFrameColor = (frame: AchievementFrame) => {
    switch (frame) {
      case 'CHALLENGE':
        return 'purple'
      case 'GOAL':
        return 'amber'
      case 'TASK':
      default:
        return 'emerald'
    }
  }

  const getFrameLabel = (frame: AchievementFrame) => {
    switch (frame) {
      case 'CHALLENGE':
        return 'Challenge'
      case 'GOAL':
        return 'Goal'
      case 'TASK':
      default:
        return 'Task'
    }
  }

  const clearFilters = () => {
    setSelectedCategory('all')
    setSelectedFrame('all')
    setSearchQuery('')
    setSortBy('default')
  }

  return (
    <div className="space-y-8">
      {/* Global Stats Overview */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        <Card className="p-4">
          <span className="text-[11px] font-medium uppercase tracking-wider text-neutral-400">
            Total
          </span>
          <p className="mt-1 text-2xl font-bold text-white">
            {achievements.length}
          </p>
          <span className="text-[10px] text-neutral-500">All advancements</span>
        </Card>

        <Card className="p-4">
          <span className="text-[11px] font-medium uppercase tracking-wider text-emerald-400">
            Tasks
          </span>
          <p className="mt-1 text-2xl font-bold text-white">
            {statsCounts.tasks}
          </p>
          <span className="text-[10px] text-neutral-500">Regular milestones</span>
        </Card>

        <Card className="p-4">
          <span className="text-[11px] font-medium uppercase tracking-wider text-amber-400">
            Goals
          </span>
          <p className="mt-1 text-2xl font-bold text-white">
            {statsCounts.goals}
          </p>
          <span className="text-[10px] text-neutral-500">Major milestones</span>
        </Card>

        <Card className="p-4">
          <span className="text-[11px] font-medium uppercase tracking-wider text-purple-400">
            Challenges
          </span>
          <p className="mt-1 text-2xl font-bold text-white">
            {statsCounts.challenges}
          </p>
          <span className="text-[10px] text-neutral-500">Elite feats</span>
        </Card>

        <Card className="p-4">
          <span className="text-[11px] font-medium uppercase tracking-wider text-neutral-400">
            Unlocks
          </span>
          <p className="mt-1 text-2xl font-bold text-white">
            {globalStats?.totalCompletions ?? 0}
          </p>
          <span className="text-[10px] text-neutral-500">Total unlocked</span>
        </Card>

        <Card className="p-4">
          <span className="text-[11px] font-medium uppercase tracking-wider text-neutral-400">
            Tracked
          </span>
          <p className="mt-1 text-2xl font-bold text-white">
            {globalStats?.trackedPlayers ?? 0}
          </p>
          <span className="text-[10px] text-neutral-500">Server players</span>
        </Card>
      </div>

      {/* Filter and Search Bar */}
      <div className="space-y-4 rounded-xl border border-neutral-800 bg-[#121215] p-4 sm:p-5">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="relative flex-1">
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="Search achievements by name or description..."
              className="w-full rounded-lg border border-neutral-800 bg-white/[0.02] px-3.5 py-2 text-xs text-white placeholder-neutral-500 transition-colors focus:border-neutral-600 focus:outline-none sm:text-sm"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-neutral-400 hover:text-white"
              >
                ✕
              </button>
            )}
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs text-neutral-400">Sort:</span>
            <select
              value={sortBy}
              onChange={e => setSortBy(e.target.value as SortOption)}
              className="rounded-lg border border-neutral-800 bg-white/[0.02] px-3 py-1.5 text-xs text-neutral-300 focus:border-neutral-600 focus:outline-none"
            >
              <option value="default" className="bg-[#121215]">
                Server Order
              </option>
              <option value="most-completed" className="bg-[#121215]">
                Most Completed
              </option>
              <option value="rarest" className="bg-[#121215]">
                Rarest First
              </option>
              <option value="alphabetical" className="bg-[#121215]">
                Alphabetical
              </option>
            </select>
          </div>
        </div>

        {/* Category Pills */}
        <div className="flex flex-wrap items-center gap-1.5">
          {availableCategories.map(cat => {
            const isActive = selectedCategory === cat.id
            return (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                  isActive
                    ? 'border border-neutral-700 bg-white/[0.08] text-white'
                    : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:border-neutral-800 hover:text-white'
                }`}
              >
                {cat.label}
              </button>
            )
          })}
        </div>

        {/* Frame Type Pills */}
        <div className="flex flex-wrap items-center gap-1.5 border-t border-neutral-800/80 pt-3">
          <button
            type="button"
            onClick={() => setSelectedFrame('all')}
            className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
              selectedFrame === 'all'
                ? 'border border-neutral-700 bg-white/[0.08] text-white'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
            }`}
          >
            All Types
          </button>
          <button
            type="button"
            onClick={() => setSelectedFrame('TASK')}
            className={`flex items-center gap-x-1.5 rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
              selectedFrame === 'TASK'
                ? 'border border-emerald-700/50 bg-emerald-500/20 text-emerald-300'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
            }`}
          >
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
            Tasks
          </button>
          <button
            type="button"
            onClick={() => setSelectedFrame('GOAL')}
            className={`flex items-center gap-x-1.5 rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
              selectedFrame === 'GOAL'
                ? 'border border-amber-700/50 bg-amber-500/20 text-amber-300'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
            }`}
          >
            <span className="h-1.5 w-1.5 rounded-full bg-amber-400" />
            Goals
          </button>
          <button
            type="button"
            onClick={() => setSelectedFrame('CHALLENGE')}
            className={`flex items-center gap-x-1.5 rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
              selectedFrame === 'CHALLENGE'
                ? 'border border-purple-700/50 bg-purple-500/20 text-purple-300'
                : 'border border-transparent bg-white/[0.02] text-neutral-400 hover:text-white'
            }`}
          >
            <span className="h-1.5 w-1.5 rounded-full bg-purple-400" />
            Challenges
          </button>
        </div>
      </div>

      {/* Results Header */}
      <div className="flex items-center justify-between text-xs text-neutral-400">
        <span>
          Showing{' '}
          <strong className="text-white">
            {filteredAndSortedAchievements.length}
          </strong>{' '}
          of {achievements.length} achievements
        </span>
        {(selectedCategory !== 'all' ||
          selectedFrame !== 'all' ||
          searchQuery.trim() !== '' ||
          sortBy !== 'default') && (
          <button
            type="button"
            onClick={clearFilters}
            className="text-xs text-neutral-400 underline hover:text-white"
          >
            Reset filters
          </button>
        )}
      </div>

      {/* Achievements Cards Grid */}
      {filteredAndSortedAchievements.length > 0 ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredAndSortedAchievements.map(adv => {
            const frameColor = getFrameColor(adv.frame)
            const frameLabel = getFrameLabel(adv.frame)
            const pct = adv.completedPercentage ?? 0
            const count = adv.completedCount ?? 0

            return (
              <Card
                key={adv.id}
                className="flex flex-col justify-between transition-colors hover:border-neutral-700"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between gap-x-2">
                    <div className="flex items-center gap-1.5">
                      <Badge color={frameColor}>{frameLabel}</Badge>
                      <span className="text-[11px] text-neutral-400">
                        {adv.categoryName || adv.category}
                      </span>
                    </div>

                    <span className="text-xs font-medium text-neutral-300">
                      {pct}%
                    </span>
                  </div>

                  <div>
                    <h3 className="text-base font-semibold text-white">
                      {adv.title}
                    </h3>
                    <p className="mt-1 text-xs leading-relaxed text-neutral-400">
                      {adv.description}
                    </p>
                  </div>
                </div>

                <div className="mt-4 space-y-2 border-t border-neutral-800/80 pt-3">
                  <div className="flex items-center justify-between text-[11px] text-neutral-500">
                    <span>Server Unlocks</span>
                    <span className="font-medium text-neutral-300">
                      {count}{' '}
                      {count === 1 ? 'player' : 'players'}
                    </span>
                  </div>

                  <div className="h-1.5 w-full overflow-hidden rounded-full bg-neutral-800">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        adv.frame === 'CHALLENGE'
                          ? 'bg-purple-500'
                          : adv.frame === 'GOAL'
                            ? 'bg-amber-500'
                            : 'bg-emerald-500'
                      }`}
                      style={{
                        width: `${Math.min(100, Math.max(0, pct))}%`,
                      }}
                    />
                  </div>
                </div>
              </Card>
            )
          })}
        </div>
      ) : (
        <Card className="py-16 text-center">
          <div className="mx-auto max-w-sm space-y-3">
            <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full border border-neutral-800 bg-white/[0.02] text-neutral-400">
              <svg
                className="h-5 w-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth="1.5"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"
                />
              </svg>
            </div>
            <p className="text-sm font-medium text-white">
              No achievements found
            </p>
            <p className="text-xs text-neutral-400">
              Try adjusting your category, type filters, or search keywords.
            </p>
            <button
              type="button"
              onClick={clearFilters}
              className="mt-2 inline-flex items-center rounded-lg border border-neutral-700 bg-white/[0.04] px-3 py-1.5 text-xs font-medium text-white hover:bg-white/[0.08]"
            >
              Clear all filters
            </button>
          </div>
        </Card>
      )}
    </div>
  )
}
