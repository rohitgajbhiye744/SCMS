import { useState, useEffect } from 'react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
    PieChart, Pie, Cell, Tooltip, Legend,
    BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer,
    LineChart, Line
} from 'recharts';

export default function AdminAnalytics() {
    const { user } = useAuth();
    const [complaints, setComplaints] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchComplaints = async () => {
            try {
                const response = await api.get('/complaints');
                setComplaints(response.data);
            } catch (error) {
                console.error("Error fetching analytics data", error);
            } finally {
                setLoading(false);
            }
        };

        if (user.role === 'ROLE_ADMIN') {
            fetchComplaints();
        }
    }, [user.role]);

    if (user.role !== 'ROLE_ADMIN') {
        return <div className="p-8 text-center text-red-500 font-medium">Access Denied. Admins only.</div>;
    }

    if (loading) return <div className="p-8 text-center">Loading analytics...</div>;

    // Data Processing
    const categoryCounts = {};
    const statusCounts = {};
    const timelineCounts = {};

    complaints.forEach(c => {
        categoryCounts[c.category] = (categoryCounts[c.category] || 0) + 1;
        statusCounts[c.status] = (statusCounts[c.status] || 0) + 1;

        const date = new Date(c.dateSubmitted).toLocaleDateString();
        timelineCounts[date] = (timelineCounts[date] || 0) + 1;
    });

    const categoryData = Object.keys(categoryCounts).map(key => ({ name: key, value: categoryCounts[key] }));
    const statusData = Object.keys(statusCounts).map(key => ({ name: key, value: statusCounts[key] }));
    const timelineData = Object.keys(timelineCounts).map(key => ({ date: key, count: timelineCounts[key] }));

    const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d', '#ffc658'];

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">Analytics Dashboard</h1>
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                
                {/* Status Chart */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="text-lg font-semibold text-gray-900 mb-4">Complaints by Status</h2>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={statusData}
                                    cx="50%"
                                    cy="50%"
                                    outerRadius={80}
                                    fill="#8884d8"
                                    dataKey="value"
                                    label
                                >
                                    {statusData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Category Chart */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="text-lg font-semibold text-gray-900 mb-4">Complaints by Category</h2>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={categoryData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="name" />
                                <YAxis />
                                <Tooltip />
                                <Bar dataKey="value" fill="#3b82f6" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Timeline Chart */}
                <div className="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="text-lg font-semibold text-gray-900 mb-4">Complaint Trends Over Time</h2>
                    <div className="h-64">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={timelineData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="date" />
                                <YAxis />
                                <Tooltip />
                                <Line type="monotone" dataKey="count" stroke="#8b5cf6" strokeWidth={2} />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
                
            </div>
        </div>
    );
}
