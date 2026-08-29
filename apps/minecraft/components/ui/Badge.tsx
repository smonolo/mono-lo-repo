import type { HTMLAttributes, PropsWithChildren } from 'react'

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
  const dynamicStyle = color
    ? {
        backgroundColor: `${color}20`,
        color,
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
