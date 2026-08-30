import type { HTMLAttributes, PropsWithChildren } from 'react'
import { getMinecraftRankColor } from '@/utils/minecraft'

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  color?: string
  className?: string
}

export function Badge({
  children,
  color,
  className = '',
  style,
  ...props
}: PropsWithChildren<BadgeProps>) {
  const hexColor = color ? getMinecraftRankColor(color) : undefined

  const dynamicStyle = hexColor
    ? {
        backgroundColor: `${hexColor}20`,
        color: hexColor,
        ...style,
      }
    : style

  return (
    <span
      className={`inline-flex items-center rounded px-2 py-0.5 text-xs font-medium ${className}`}
      style={dynamicStyle}
      {...props}
    >
      {children}
    </span>
  )
}
