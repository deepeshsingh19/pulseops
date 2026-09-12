interface StatCardProps {
  label: string;
  value: number;
  description?: string;
  tone?: "default" | "danger" | "warning" | "success";
}

export default function StatCard({
  label,
  value,
  description,
  tone = "default",
}: StatCardProps) {
  return (
    <div className={`stat-card stat-card-${tone}`}>
      <div className="stat-card-label">{label}</div>

      <div className="stat-card-value">{value}</div>

      {description && (
        <div className="stat-card-description">
          {description}
        </div>
      )}
    </div>
  );
}