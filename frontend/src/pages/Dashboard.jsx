import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';
import { FileText, Clock, CheckCircle, AlertCircle, Plus } from 'lucide-react';
import { Link } from 'react-router-dom';
import {
    PieChart, Pie, Cell, Tooltip, Legend,
    BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer
} from 'recharts';

export default function Dashboard() {
    const { user } = useAuth();

    const { data: complaints = [], isLoading: loading } = useQuery({
        queryKey: ['complaints', user.role, user.departmentId],
        queryFn: async () => {
            const response = await api.get('/complaints');
            return response.data;
        }
    });

    if (loading) return <div>Loading dashboard...</div>;

    const stats = {
        total: complaints.length,
        pending: complaints.filter(c => c.status === 'Submitted' || c.status === 'Assigned').length,
        inProgress: complaints.filter(c => c.status === 'In Progress').length,
        resolved: complaints.filter(c => c.status === 'Resolved' || c.status === 'Closed').length
    };

    // Chart Data Processing for Coordinator/Admin
    const statusCounts = {};
    const categoryCounts = {};
    if (user.role === 'ROLE_DEPARTMENT_COORDINATOR' || user.role === 'ROLE_ADMIN') {
        complaints.forEach(c => {
            statusCounts[c.status] = (statusCounts[c.status] || 0) + 1;
            categoryCounts[c.category] = (categoryCounts[c.category] || 0) + 1;
        });
    }

    const statusData = Object.keys(statusCounts).map(key => ({ name: key, value: statusCounts[key] }));
    const categoryData = Object.keys(categoryCounts).map(key => ({ name: key, value: categoryCounts[key] }));
    
    // Palette: https://colorhunt.co/palette/fffbf5f7efe5c3acd0674188
    const COLORS = ['#674188', '#8655AD', '#A679CD', '#C3ACD0', '#E5D8EE'];

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-[#281B34] tracking-tight">Welcome, {user.username}</h1>
                <p className="text-[#7A668A] text-sm mt-1">Here is an overview of {user.role === 'ROLE_STAFF' ? 'your assigned tasks' : 'your complaints'}.</p>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-white p-5 rounded-xl shadow-sm border border-[#C3ACD0]/40 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-[#7A668A]">Total Complaints</p>
                        <div className="p-2 bg-[#C3ACD0]/20 rounded-lg">
                            <FileText className="h-4 w-4 text-[#674188]" />
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-[#281B34]">{stats.total}</p>
                </div>
                <div className="bg-white p-5 rounded-xl shadow-sm border border-[#C3ACD0]/40 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-[#7A668A]">Pending</p>
                        <div className="p-2 bg-[#C3ACD0]/20 rounded-lg">
                            <Clock className="h-4 w-4 text-[#674188]" />
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-[#281B34]">{stats.pending}</p>
                </div>
                <div className="bg-white p-5 rounded-xl shadow-sm border border-[#C3ACD0]/40 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-[#7A668A]">In Progress</p>
                        <div className="p-2 bg-[#C3ACD0]/20 rounded-lg">
                            <AlertCircle className="h-4 w-4 text-[#674188]" />
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-[#674188]">{stats.inProgress}</p>
                </div>
                <div className="bg-white p-5 rounded-xl shadow-sm border border-[#C3ACD0]/40 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-[#7A668A]">Resolved</p>
                        <div className="p-2 bg-[#C3ACD0]/20 rounded-lg">
                            <CheckCircle className="h-4 w-4 text-[#674188]" />
                        </div>
                    </div>
                    <p className="text-2xl font-bold text-[#281B34]">{stats.resolved}</p>
                </div>
            </div>

            {/* Analytics Charts for Manager/Admin */}
            {(user.role === 'ROLE_DEPARTMENT_COORDINATOR' || user.role === 'ROLE_ADMIN') && complaints.length > 0 && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-[#C3ACD0]/40">
                        <h2 className="text-sm font-semibold text-[#281B34] mb-4">Complaints by Status</h2>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={statusData}
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={80}
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
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-[#C3ACD0]/40">
                        <h2 className="text-sm font-semibold text-[#281B34] mb-4">Complaints by Category</h2>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={categoryData}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E8DEC8" />
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#7A668A', fontSize: 12}} />
                                    <YAxis axisLine={false} tickLine={false} tick={{fill: '#7A668A', fontSize: 12}} />
                                    <Tooltip cursor={{fill: '#F7EFE5'}} />
                                    <Bar dataKey="value" fill="#674188" radius={[6, 6, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>
            )}

            <div className="bg-white rounded-xl shadow-sm border border-[#C3ACD0]/40 overflow-hidden">
                <div className="px-6 py-4 border-b border-[#C3ACD0]/40 bg-[#F7EFE5]/70 flex justify-between items-center">
                    <h2 className="text-sm font-semibold text-[#281B34]">
                        {user.role === 'ROLE_REPORTER' ? 'My Complaints' : user.role === 'ROLE_STAFF' ? 'Assigned Tasks' : 'Recent Complaints'}
                    </h2>
                    {user.role === 'ROLE_REPORTER' && (
                        <Link to="/complaints/new" className="bg-[#674188] text-white px-3 py-1.5 text-xs font-medium rounded-lg hover:bg-[#543372] transition-colors inline-flex items-center shadow-sm">
                            <Plus className="h-3.5 w-3.5 mr-1" /> New Complaint
                        </Link>
                    )}
                </div>
                <div className="divide-y divide-[#C3ACD0]/30">
                    {complaints.length === 0 ? (
                        <div className="p-6 text-center text-sm text-[#7A668A]">No complaints found.</div>
                    ) : (
                        (user.role === 'ROLE_REPORTER' ? complaints : complaints.slice(0, 5)).map(complaint => (
                            <Link to={`/complaints/${complaint.id}`} key={complaint.id} className="p-6 flex items-center justify-between hover:bg-[#F7EFE5]/40 transition-colors block">
                                <div>
                                    <h3 className="text-sm font-medium text-[#281B34]">{complaint.title}</h3>
                                    <p className="text-xs text-[#7A668A] mt-1">{complaint.category} • {new Date(complaint.dateSubmitted).toLocaleDateString()}</p>
                                </div>
                                <div>
                                    <div className="flex items-center space-x-3">
                                        <span className={`px-2.5 py-0.5 text-xs font-medium rounded-full border ${
                                            complaint.status === 'Resolved' || complaint.status === 'Closed' ? 'bg-[#F7EFE5] text-[#674188] border-[#C3ACD0]' :
                                            complaint.status === 'In Progress' ? 'bg-[#674188] text-white border-[#674188]' :
                                            'bg-[#C3ACD0]/20 text-[#543372] border-[#C3ACD0]'
                                        }`}>
                                            {complaint.status}
                                        </span>
                                    </div>
                                </div>
                            </Link>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}
